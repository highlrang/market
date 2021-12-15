package com.myproject.myweb.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.myproject.myweb.config.AwsSesConfig;
import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.exception.AwsSesMailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {
    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public void send(SenderDto senderDto) {
        log.info("Attempting to send an email via AWS SES");

        SendEmailRequest sendEmailRequest = senderDto.toSendRequestDto();
        SendEmailResult sendEmailResult =
                amazonSimpleEmailService.sendEmail(sendEmailRequest);

        if(sendEmailResult.getSdkHttpMetadata().getHttpStatusCode() == 200) {
            log.info("Email sent complete");
        }else{
            log.error("Email sent fail > TO. " + senderDto.getTo().get(0) +
                    " > Detail. " + sendEmailResult.getSdkResponseMetadata());
        }
    }

    public void createTemplate(String templateName, String subject, String textPart, String htmlPart){
        Template template = new Template();
        template.setTemplateName(templateName);
        template.setSubjectPart(subject);
        template.setTextPart(textPart);
        template.setHtmlPart(htmlPart);
        CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
        amazonSimpleEmailService.createTemplate(request);
    }

    public void updateTemplate(String templateName, String subject, String text, String html){
        Template template = new Template();
        template.setTemplateName(templateName);
        template.setSubjectPart(subject);
        template.setTextPart(text);
        template.setHtmlPart(html);
        UpdateTemplateRequest reqeust = new UpdateTemplateRequest().withTemplate(template);
        amazonSimpleEmailService.updateTemplate(reqeust);
    }

    public void sendTemplate(SenderDto senderDto){
        SendTemplatedEmailRequest request = null;
        try {
            request = senderDto.toSendTemplatedRequestDto();
        }catch (JsonProcessingException e) {
            log.error("Email Sent Fail " + senderDto.getTo().get(0));
            log.error("Because of Converting Map to Json String");
            throw new AwsSesMailSendingException();
        }
        SendTemplatedEmailResult sendTemplatedEmailResult =
                amazonSimpleEmailService.sendTemplatedEmail(request);

        if(sendTemplatedEmailResult.getSdkHttpMetadata().getHttpStatusCode() == 200){
            log.info("Email Sent Complete " + senderDto.getTo().get(0));
        }else{
            log.error("Email Sent Fail " + senderDto.getTo().get(0) +
                    " > Detail. " + sendTemplatedEmailResult.getSdkResponseMetadata());
            throw new AwsSesMailSendingException();
        }
    }
}
