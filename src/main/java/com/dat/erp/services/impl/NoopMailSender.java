package com.dat.erp.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dat.erp.services.SystemMailSender;

public class NoopMailSender implements SystemMailSender {
    private static final Logger log = LoggerFactory.getLogger(NoopMailSender.class);

    @Override
    public void sendHtml(String from, String to, String subject, String htmlBody) {
        log.warn("Mail is not configured (skipping send) to={} subject={}", to, subject);
        throw new IllegalStateException("Mail is not configured");
    }
}
