package com.dat.erp.services.impl;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.dat.erp.dto.request.EmailRequest;
import com.dat.erp.services.EmailTemplateEngine;

@Service
public class ThymeleafEmailTemplateEngine implements EmailTemplateEngine {
    private final SpringTemplateEngine templateEngine;

    public ThymeleafEmailTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String renderHtmlTemplate(EmailRequest request) {
        Context context = new Context();
        context.setVariable("fullName", request.getFullName());
        context.setVariable("gender", request.getGender());
        context.setVariable("greeting", buildGreeting(request.getFullName(), request.getGender()));
        return templateEngine.process(resolveTemplateName(request.getHtmlFilePath()), context);
    }

    private static String resolveTemplateName(String htmlFilePath) {
        if (htmlFilePath == null) {
            return null;
        }
        String templateName = htmlFilePath.trim().replace("\\", "/");
        if (templateName.startsWith("/")) {
            templateName = templateName.substring(1);
        }
        if (templateName.startsWith("templates/")) {
            templateName = templateName.substring("templates/".length());
        }
        if (templateName.endsWith(".html")) {
            templateName = templateName.substring(0, templateName.length() - ".html".length());
        }
        return templateName;
    }

    private static String buildGreeting(String fullName, String gender) {
        String safeName = fullName == null ? "" : fullName.trim();
        if (safeName.isBlank()) {
            safeName = "there";
        }
        String prefix = salutationPrefix(gender);
        if (prefix.isBlank()) {
            return "Dear " + safeName;
        }
        return "Dear " + prefix + " " + safeName;
    }

    private static String salutationPrefix(String gender) {
        if (gender == null) {
            return "";
        }
        String normalized = gender.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.equals("male") || normalized.equals("m") || normalized.equals("man") || normalized.equals("mr")) {
            return "Mr.";
        }
        if (normalized.equals("female") || normalized.equals("f") || normalized.equals("woman")
                || normalized.equals("ms") || normalized.equals("mrs")) {
            return "Ms.";
        }
        return "";
    }
}

