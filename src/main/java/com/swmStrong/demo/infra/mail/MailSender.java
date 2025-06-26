package com.swmStrong.demo.infra.mail;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.swmStrong.demo.infra.mail.dto.SendMailDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MailSender {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final EmailTemplateLoader emailTemplateLoader;

    public MailSender(AmazonSimpleEmailService amazonSimpleEmailService, EmailTemplateLoader emailTemplateLoader) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
        this.emailTemplateLoader = emailTemplateLoader;
    }

    public void send(SendMailDto sendMailDto) {
        try {
            amazonSimpleEmailService.sendEmail(sendMailDto.toSendRequestDto());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendWelcomeEmail(String email, String username) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            //TODO: 언젠가, 여기를 블로그 url로 바꾸고, 활동 확인하기? 같은 문구로 바꿔보기
            variables.put("app_url", "https://pawcus.dev");

            String content = emailTemplateLoader.loadTemplate(EmailTemplate.WELCOME, variables);

            SendMailDto sendMailDto = SendMailDto.builder()
                    .to(email)
                    .subject(EmailTemplate.WELCOME.getSubject())
                    .content(content)
                    .build();

            send(sendMailDto);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to: " + email + ", error: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String email, String username, String resetUrl, String expiryTime) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("reset_url", resetUrl);
            variables.put("expiry_time", expiryTime);

            String content = emailTemplateLoader.loadTemplate(EmailTemplate.PASSWORD_RESET, variables);

            SendMailDto sendMailDto = SendMailDto.builder()
                    .to(email)
                    .subject(EmailTemplate.PASSWORD_RESET.getSubject())
                    .content(content)
                    .build();

            send(sendMailDto);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + email + ", error: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String email, String username, String verificationCode, String verificationUrl, String expiryTime) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("verification_code", verificationCode);
            variables.put("verification_url", verificationUrl);
            variables.put("expiry_time", expiryTime);

            String content = emailTemplateLoader.loadTemplate(EmailTemplate.EMAIL_VERIFICATION, variables);

            SendMailDto sendMailDto = SendMailDto.builder()
                    .to(email)
                    .subject(EmailTemplate.EMAIL_VERIFICATION.getSubject())
                    .content(content)
                    .build();

            send(sendMailDto);
        } catch (Exception e) {
            System.err.println("Failed to send verification email to: " + email + ", error: " + e.getMessage());
        }
    }
}
