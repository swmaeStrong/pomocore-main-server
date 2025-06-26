package com.swmStrong.demo.infra.mail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class EmailTemplateLoader {

    public String loadTemplate(EmailTemplate template, Map<String, String> variables) {
        try {
            ClassPathResource resource = new ClassPathResource(template.getTemplatePath());
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template: " + template.getTemplatePath(), e);
        }
    }
}