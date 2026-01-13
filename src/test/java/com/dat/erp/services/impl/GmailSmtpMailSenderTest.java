package com.dat.erp.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

class GmailSmtpMailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    private GmailSmtpMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mailSender = new GmailSmtpMailSender(javaMailSender);
    }

    @Test
    void sendHtml_buildsAndSendsMimeMessage() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailSender.sendHtml("from@example.com", "to@example.com", "Subject", "<b>Hello</b>");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();
        assertThat(sent.getSubject()).isEqualTo("Subject");
        assertThat(sent.getAllRecipients()).isNotNull();
        assertThat(sent.getAllRecipients()[0].toString()).isEqualTo("to@example.com");
        assertThat(sent.getFrom()).isNotNull();
        assertThat(sent.getFrom()[0].toString()).contains("from@example.com");
        assertThat(sent.getContent().toString()).contains("<b>Hello</b>");
    }

    @Test
    void sendHtml_blankFrom_doesNotSetFromHeader() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailSender.sendHtml("  ", "to@example.com", "Subject", "<b>Hello</b>");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());

        MimeMessage sent = captor.getValue();
        assertThat(sent.getFrom() == null || sent.getFrom().length == 0).isTrue();
    }
}

