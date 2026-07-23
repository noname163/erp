package com.dat.erp.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.dat.erp.dto.request.EmailRequest;

class ThymeleafEmailTemplateEngineTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    private ThymeleafEmailTemplateEngine engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        engine = new ThymeleafEmailTemplateEngine(templateEngine);
    }

    @Test
    void renderHtmlTemplate_resolvesTemplateNameAndSetsVariables() {
        EmailRequest request = new EmailRequest();
        request.setFullName("Jane Doe");
        request.setGender("female");
        request.setHtmlFilePath("templates/email/create-account.html");
        request.setTemplateVariables(Map.of("companyName", "OpenAI"));

        when(templateEngine.process(eq("email/create-account"), any(Context.class))).thenReturn("<html/>");

        String html = engine.renderHtmlTemplate(request);

        assertThat(html).isEqualTo("<html/>");

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/create-account"), captor.capture());

        Context ctx = captor.getValue();
        assertThat(ctx.getVariable("fullName")).isEqualTo("Jane Doe");
        assertThat(ctx.getVariable("gender")).isEqualTo("female");
        assertThat(ctx.getVariable("companyName")).isEqualTo("OpenAI");
        assertThat(ctx.getVariable("greeting").toString()).contains("Ms.");
    }

    @Test
    void renderHtmlTemplate_blankName_usesThereInGreeting() {
        EmailRequest request = new EmailRequest();
        request.setFullName("   ");
        request.setGender("male");
        request.setHtmlFilePath("create-account.html");

        when(templateEngine.process(eq("create-account"), any(Context.class))).thenReturn("<html/>");

        engine.renderHtmlTemplate(request);

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("create-account"), captor.capture());
        assertThat(captor.getValue().getVariable("greeting").toString()).contains("there");
    }
}

