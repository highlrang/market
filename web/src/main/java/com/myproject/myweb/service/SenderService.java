package com.myproject.myweb.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.myproject.myweb.config.AwsSesConfig;
import com.myproject.myweb.dto.SenderDto;
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

    public void createTemplate(String subject, String templateName, String textPart, String htmlPart){
        Template template = new Template();
        template.setSubjectPart(subject);
        template.setTemplateName(templateName);
        template.setTextPart(textPart);
        template.setHtmlPart(htmlPart);
        CreateTemplateRequest request = new CreateTemplateRequest().withTemplate(template);
        amazonSimpleEmailService.createTemplate(request);
    }

    public void sendTemplate(SenderDto senderDto){
        SendTemplatedEmailRequest request = senderDto.toSendTemplatedRequestDto();
        SendTemplatedEmailResult sendTemplatedEmailResult =
                amazonSimpleEmailService.sendTemplatedEmail(request);

        if(sendTemplatedEmailResult.getSdkHttpMetadata().getHttpStatusCode() == 200){
            log.info("Email Sent Complete " + senderDto.getTo().get(0));
        }else{
            log.error("Email Sent Fail " + senderDto.getTo().get(0) +
                    " > Detail. " + sendTemplatedEmailResult.getSdkResponseMetadata());
        }
    }
}