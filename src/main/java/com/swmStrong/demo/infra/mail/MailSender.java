package com.swmStrong.demo.infra.mail;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.infra.mail.dto.SendMailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        } catch (AmazonServiceException e) {
            log.error("AWS SES service error while sending email to {}: {}", sendMailDto.getTo(), e.getMessage());
            throw new ApiException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", sendMailDto.getTo(), e.getMessage());
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void sendWelcomeEmail(String email, String username) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("blog_url", "https://blog.pawcus.dev");

            String content = emailTemplateLoader.loadTemplate(EmailTemplate.WELCOME, variables);

            SendMailDto sendMailDto = SendMailDto.builder()
                    .to(email)
                    .subject(EmailTemplate.WELCOME.getSubject())
                    .content(content)
                    .build();

            send(sendMailDto);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {} - {}", email, e.getMessage());
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
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
            log.error("Failed to send password reset email to: {} - {}", email, e.getMessage());
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
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
            log.error("Failed to send verification email to: {} - {}", email, e.getMessage());
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
