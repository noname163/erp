package com.dat.erp.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.entities.Email;
import com.dat.erp.mapper.interfaces.EmailMapper;
import com.dat.erp.repositories.customrepositories.EmailRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmailTemplateEngine;
import com.dat.erp.services.SystemMailSender;

class EmailServiceImplTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private EmailTemplateEngine emailTemplateEngine;

    @Mock
    private SystemMailSender systemMailSender;

    @Mock
    private CodeGenerator codeGenerator;

    private EmailServiceImpl emailService;

    private EmailRequest request;
    private Email mappedEmail;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailServiceImpl(emailRepository, emailMapper, emailTemplateEngine, systemMailSender,
                codeGenerator, 3,
                "default-from@example.com");

        request = new EmailRequest();
        request.setFrom("request-from@example.com");
        request.setTo("to@example.com");
        request.setFullName("Jane Doe");
        request.setGender("female");
        request.setHtmlFilePath("templates/mail/create-account.html");

        mappedEmail = new Email();
        mappedEmail.setEmailFrom(request.getFrom());
        mappedEmail.setEmailTo(request.getTo());
        mappedEmail.setFullName(request.getFullName());
        mappedEmail.setGender(request.getGender());
        mappedEmail.setHtmlFilePath(request.getHtmlFilePath());

        when(emailRepository.save(any(Email.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailMapper.toEntity(any(EmailRequest.class))).thenReturn(mappedEmail);
        when(codeGenerator.nextCode("EML-")).thenReturn("EML-000001");
        when(emailTemplateEngine.renderHtmlTemplate(any(EmailRequest.class))).thenReturn("<html>Hello</html>");
    }

    @Test
    void sendCreateAccountMail_success_marksSentAndDoesNotRetry() {
        emailService.sendCreateAccountMail(request);

        verify(emailTemplateEngine).renderHtmlTemplate(eq(request));
        verify(systemMailSender).sendHtml(eq("request-from@example.com"), eq("to@example.com"), eq("Create account"),
                eq("<html>Hello</html>"));

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(2)).save(captor.capture());
        Email finalSaved = captor.getAllValues().get(1);

        assertThat(finalSaved.isSent()).isTrue();
        assertThat(finalSaved.isNeedRetry()).isFalse();
        assertThat(finalSaved.getErrorMessage()).isNull();
        assertThat(finalSaved.getRetryTime()).isEqualTo(0);
        assertThat(finalSaved.getSubject()).isEqualTo("Create account");
        assertThat(finalSaved.getCode()).isEqualTo("EML-000001");
    }

    @Test
    void sendCreateAccountMail_existingCode_keepsCodeAndDoesNotGenerateAnother() {
        com.dat.erp.testutils.EntityTestData.setCode(mappedEmail, "EML-EXISTING");

        emailService.sendCreateAccountMail(request);

        assertThat(mappedEmail.getCode()).isEqualTo("EML-EXISTING");
        verify(codeGenerator, never()).nextCode(any());
    }

    @Test
    void sendCreateAccountMail_requestFromBlank_usesDefaultFrom() {
        request.setFrom("  ");

        emailService.sendCreateAccountMail(request);

        verify(systemMailSender).sendHtml(eq("default-from@example.com"), eq("to@example.com"), eq("Create account"),
                eq("<html>Hello</html>"));
    }

    @Test
    void sendCreateAccountMail_failure_marksRetryAndStoresError() {
        doThrow(new RuntimeException("smtp down")).when(systemMailSender).sendHtml(any(), any(), any(), any());

        emailService.sendCreateAccountMail(request);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(emailRepository, times(2)).save(captor.capture());
        Email finalSaved = captor.getAllValues().get(1);

        assertThat(finalSaved.isSent()).isFalse();
        assertThat(finalSaved.isNeedRetry()).isTrue();
        assertThat(finalSaved.getRetryTime()).isEqualTo(1);
        assertThat(finalSaved.getErrorMessage()).contains("smtp down");
    }

    @Test
    void retryPendingEmails_maxRetryZero_doesNothing() {
        EmailServiceImpl service = new EmailServiceImpl(emailRepository, emailMapper, emailTemplateEngine,
                systemMailSender, codeGenerator, 0, "default-from@example.com");

        service.retryPendingEmails();

        verify(emailRepository, never()).findTop50ByNeedRetryAndSentFalseOrderByIdAsc(true);
        verify(emailRepository, never()).save(any());
        verify(systemMailSender, never()).sendHtml(any(), any(), any(), any());
    }

    @Test
    void retryPendingEmails_retryLimitReached_clearsNeedRetryWithoutSending() {
        Email email = new Email();
        com.dat.erp.testutils.EntityTestData.setId(email, 1L);
        email.setNeedRetry(true);
        email.setSent(false);
        email.setRetryTime(3);

        when(emailRepository.findTop50ByNeedRetryAndSentFalseOrderByIdAsc(true)).thenReturn(List.of(email));

        emailService.retryPendingEmails();

        verify(systemMailSender, never()).sendHtml(any(), any(), any(), any());
        verify(emailRepository).save(email);
        assertThat(email.isNeedRetry()).isFalse();
    }

    @Test
    void retryPendingEmails_success_sendsAndMarksSent() {
        Email email = new Email();
        com.dat.erp.testutils.EntityTestData.setId(email, 1L);
        email.setNeedRetry(true);
        email.setSent(false);
        email.setRetryTime(0);
        email.setEmailFrom(null);
        email.setEmailTo("to@example.com");
        email.setFullName("Jane Doe");
        email.setGender("female");
        email.setSubject("Create account");
        email.setHtmlFilePath("templates/mail/create-account.html");

        when(emailRepository.findTop50ByNeedRetryAndSentFalseOrderByIdAsc(true)).thenReturn(List.of(email));

        emailService.retryPendingEmails();

        verify(systemMailSender).sendHtml(eq("default-from@example.com"), eq("to@example.com"), eq("Create account"),
                eq("<html>Hello</html>"));
        assertThat(email.isSent()).isTrue();
        assertThat(email.isNeedRetry()).isFalse();
        assertThat(email.getErrorMessage()).isNull();
    }

    @Test
    void retryPendingEmails_failure_incrementsRetryAndStopsRetryAtLimit() {
        EmailServiceImpl service = new EmailServiceImpl(emailRepository, emailMapper, emailTemplateEngine,
                systemMailSender, codeGenerator, 2, "default-from@example.com");

        Email email = new Email();
        com.dat.erp.testutils.EntityTestData.setId(email, 1L);
        email.setNeedRetry(true);
        email.setSent(false);
        email.setRetryTime(1);
        email.setEmailFrom("from@example.com");
        email.setEmailTo("to@example.com");
        email.setFullName("Jane Doe");
        email.setGender("female");
        email.setSubject("Create account");
        email.setHtmlFilePath("templates/mail/create-account.html");

        when(emailRepository.findTop50ByNeedRetryAndSentFalseOrderByIdAsc(true)).thenReturn(List.of(email));
        doThrow(new RuntimeException("smtp down")).when(systemMailSender).sendHtml(any(), any(), any(), any());

        service.retryPendingEmails();

        assertThat(email.getRetryTime()).isEqualTo(2);
        assertThat(email.isNeedRetry()).isFalse();
        assertThat(email.getErrorMessage()).contains("smtp down");
    }
}
