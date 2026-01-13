package com.dat.erp.systemconfigs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import com.dat.erp.services.SystemMailSender;
import com.dat.erp.services.impl.GmailSmtpMailSender;

@Configuration
public class MailSenderConfig {
    @Bean
    @ConditionalOnMissingBean(SystemMailSender.class)
    public SystemMailSender systemMailSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        return new GmailSmtpMailSender(mailSender);
    }

}
