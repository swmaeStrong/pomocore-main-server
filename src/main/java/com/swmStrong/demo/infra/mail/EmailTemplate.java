package com.swmStrong.demo.infra.mail;

public enum EmailTemplate {
    WELCOME("Welcome to Pawcus!", "templates/welcome-template.html"),
    PASSWORD_RESET("Reset Your Password", "templates/password-reset-template.html"),
    EMAIL_VERIFICATION("Verify Your Email", "templates/email-verification-template.html");

    private final String subject;
    private final String templatePath;

    EmailTemplate(String subject, String templatePath) {
        this.subject = subject;
        this.templatePath = templatePath;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplatePath() {
        return templatePath;
    }
}