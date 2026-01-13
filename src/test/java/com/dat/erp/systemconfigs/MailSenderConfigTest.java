package com.dat.erp.systemconfigs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import com.dat.erp.services.SystemMailSender;
import com.dat.erp.services.impl.GmailSmtpMailSender;

class MailSenderConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MailSenderConfig.class);

    @Test
    void whenNoJavaMailSender_thenNoopMailSenderIsProvided() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SystemMailSender.class);
        });
    }

    @Test
    void whenJavaMailSenderPresent_thenGmailSmtpMailSenderIsProvided() {
        contextRunner.withBean(JavaMailSender.class, () -> Mockito.mock(JavaMailSender.class)).run(context -> {
            assertThat(context).hasSingleBean(SystemMailSender.class);
            assertThat(context.getBean(SystemMailSender.class)).isInstanceOf(GmailSmtpMailSender.class);
        });
    }
}
