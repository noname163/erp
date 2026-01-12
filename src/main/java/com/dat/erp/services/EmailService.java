package com.dat.erp.services;

import com.dat.erp.dto.request.EmailRequest;

public interface EmailService {
    public void sendCreateAccountMail(EmailRequest request);

    public void retryPendingEmails();
}
