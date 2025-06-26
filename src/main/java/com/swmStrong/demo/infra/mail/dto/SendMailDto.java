package com.swmStrong.demo.infra.mail.dto;

import com.amazonaws.services.simpleemail.model.*;
import lombok.Builder;

public class SendMailDto {
    private String from = "Pawcus <no-reply@pawcus.dev>";
    private String to;
    private String subject;
    private String content;


    @Builder
    public SendMailDto(String to, String subject, String content) {
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public SendEmailRequest toSendRequestDto() {
        Destination destination = new Destination().withToAddresses(to);
        Message message = new Message()
                .withSubject(createContent(subject))
                .withBody(createBody(content));
        return new SendEmailRequest()
                .withSource(from)
                .withDestination(destination)
                .withMessage(message);
    }

    private Content createContent(String text) {
        return new Content().withCharset("utf-8").withData(text);
    }

    private Body createBody(String text) {
        return new Body().withHtml(createContent(text));
    }
}