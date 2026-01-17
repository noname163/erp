package com.dat.erp.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.entities.Email;
import com.dat.erp.mapper.interfaces.EmailMapper;
import com.dat.erp.repositories.customrepositories.EmailRepository;
import com.dat.erp.services.EmailService;
import com.dat.erp.services.EmailTemplateEngine;
import com.dat.erp.services.SystemMailSender;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final boolean NEED_RETRY_YES = true;
    private static final boolean NEED_RETRY_NO = false;

    private final EmailRepository emailRepository;
    private final EmailMapper emailMapper;
    private final EmailTemplateEngine emailTemplateEngine;
    private final SystemMailSender systemMailSender;
    private final int maxRetry;
    private final String defaultFrom;

    public EmailServiceImpl(
            EmailRepository emailRepository,
            EmailMapper emailMapper,
            EmailTemplateEngine emailTemplateEngine,
            SystemMailSender systemMailSender,
            @Value("${email.maxRetry:3}") int maxRetry,
            @Value("${email.defaultFrom:}") String defaultFrom) {
        this.emailRepository = emailRepository;
        this.emailMapper = emailMapper;
        this.emailTemplateEngine = emailTemplateEngine;
        this.systemMailSender = systemMailSender;
        this.maxRetry = Math.max(0, maxRetry);
        this.defaultFrom = defaultFrom;
    }

    @Async("emailTaskExecutor")
    @Transactional
    @Override
    public void sendCreateAccountMail(EmailRequest request) {
        Email email = emailMapper.toEntity(request);
        email.setCode("EML-" + UUID.randomUUID());
        email.setSubject("Create account");
        email.setRetryTime(0);
        email.setNeedRetry(NEED_RETRY_NO);
        email.setSent(false);

        Email saved = emailRepository.save(email);

        try {
            String html = emailTemplateEngine.renderHtmlTemplate(request);
            String from = resolveFrom(request.getFrom());
            systemMailSender.sendHtml(from, request.getTo(), email.getSubject(), html);

            saved.setSent(true);
            saved.setNeedRetry(NEED_RETRY_NO);
            saved.setErrorMessage(null);
            log.info("AUDIT action=SEND_ACCOUNT_EMAIL result=SUCCESS to={} template={}", request.getTo(),
                    request.getHtmlFilePath());
        } catch (Exception e) {
            log.warn("AUDIT action=SEND_ACCOUNT_EMAIL result=FAILED to={} template={} error={}", request.getTo(),
                    request.getHtmlFilePath(), e.getMessage());
            applyFailure(saved, e);
        }

        emailRepository.save(saved);
    }

    @Transactional
    @Override
    public void retryPendingEmails() {
        if (maxRetry <= 0) {
            return;
        }
        for (Email email : emailRepository.findTop50ByNeedRetryAndIsSentFalseOrderByIdAsc(NEED_RETRY_YES)) {
            int currentRetry = email.getRetryTime() == null ? 0 : email.getRetryTime();
            if (currentRetry >= maxRetry) {
                email.setNeedRetry(NEED_RETRY_NO);
                emailRepository.save(email);
                continue;
            }
            try {
                EmailRequest request = new EmailRequest();
                request.setFrom(email.getEmailFrom());
                request.setTo(email.getEmailTo());
                request.setFullName(email.getFullName());
                request.setGender(email.getGender());
                request.setHtmlFilePath(email.getHtmlFilePath());

                String html = emailTemplateEngine.renderHtmlTemplate(request);
                systemMailSender.sendHtml(resolveFrom(request.getFrom()), request.getTo(), email.getSubject(), html);

                email.setSent(true);
                email.setNeedRetry(NEED_RETRY_NO);
                email.setErrorMessage(null);
                log.info("AUDIT action=SEND_ACCOUNT_EMAIL result=SUCCESS to={} template={} retry={}", request.getTo(),
                        request.getHtmlFilePath(), email.getRetryTime());
            } catch (Exception e) {
                log.warn("AUDIT action=SEND_ACCOUNT_EMAIL result=FAILED id={} to={} template={} retry={} error={}",
                        email.getId(), email.getEmailTo(), email.getHtmlFilePath(), email.getRetryTime(), e.getMessage());
                applyFailure(email, e);
            }
            emailRepository.save(email);
        }
    }

    private void applyFailure(Email email, Exception e) {
        email.setSent(false);
        email.setErrorMessage(e.getMessage());

        int currentRetry = email.getRetryTime() == null ? 0 : email.getRetryTime();
        int nextRetry = currentRetry + 1;
        email.setRetryTime(nextRetry);

        if (maxRetry > 0 && nextRetry >= maxRetry) {
            email.setNeedRetry(NEED_RETRY_NO);
        } else {
            email.setNeedRetry(NEED_RETRY_YES);
        }
    }

    private String resolveFrom(String requestFrom) {
        if (requestFrom != null && !requestFrom.isBlank()) {
            return requestFrom;
        }
        if (defaultFrom != null && !defaultFrom.isBlank()) {
            return defaultFrom;
        }
        return null;
    }
}
