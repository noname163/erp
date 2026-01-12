package com.dat.erp.services;

public interface SystemMailSender {
    public void sendHtml(String from, String to, String subject, String htmlBody);
}

