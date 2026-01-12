package com.dat.erp.services;

import com.dat.erp.dto.request.EmailRequest;

public interface EmailTemplateEngine {
    public String renderHtmlTemplate(EmailRequest request);
}

