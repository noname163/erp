package com.dat.erp.services.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dat.erp.services.EmailService;

@Component
@ConditionalOnProperty(name = "email.retry.enabled", havingValue = "true")
public class EmailRetryJob {
    private final EmailService emailService;

    public EmailRetryJob(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedDelayString = "${email.retry.fixedDelayMs:60000}")
    public void run() {
        emailService.retryPendingEmails();
    }
}

