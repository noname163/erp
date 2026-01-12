package com.dat.erp.systemconfigs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.dat.erp.services.SystemMailSender;
import com.dat.erp.services.impl.GmailSmtpMailSender;
import com.dat.erp.services.impl.NoopMailSender;

@Configuration
public class MailSenderConfig {
    @Bean
    @ConditionalOnBean(JavaMailSender.class)
    public SystemMailSender gmailSmtpMailSender(JavaMailSender mailSender) {
        return new GmailSmtpMailSender(mailSender);
    }

    @Bean
    @ConditionalOnMissingBean(SystemMailSender.class)
    public SystemMailSender noopMailSender() {
        return new NoopMailSender();
    }
}

