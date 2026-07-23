package com.dat.erp.services.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.dat.erp.services.EmailService;

class EmailRetryJobTest {
    @Test
    void run_delegatesToEmailService() {
        EmailService emailService = Mockito.mock(EmailService.class);
        EmailRetryJob job = new EmailRetryJob(emailService);

        job.run();

        verify(emailService).retryPendingEmails();
    }
}

