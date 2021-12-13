package com.myproject.myweb.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
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

        SendEmailResult sendEmailResult =
                amazonSimpleEmailService.sendEmail(senderDto.toSendRequestDto());

        if(sendEmailResult.getSdkHttpMetadata().getHttpStatusCode() == 200) {
            log.info("Email sent complete");
        }else{
            log.error("Email sent fail > TO. " + senderDto.getTo().get(0) + " > DETAIL. " + sendEmailResult.getSdkResponseMetadata());
        }
    }
}
