package com.myproject.myweb;

import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.service.aws.FileUploadService;
import com.myproject.myweb.service.aws.SenderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.myproject.myweb.config.Constants.JOIN_MAIL_TEMPLATE;
import static com.myproject.myweb.config.Constants.MAIL_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class AwsTest {
    @Value("${webUrl}") private String webUrl;
    @Autowired private SenderService senderService;
    @Autowired private FileUploadService fileUploadService;

    @Test
    public void aws_ses_mail_test(){
        /*
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(customer.getEmail()));
        message.setSubject(subject);
        message.setText(context, "UTF-8", "html");
        emailSender.send(message);
         */

        /*
        senderService.send(SenderDto.builder()
                .from("developoffi@gmail.com")
                .to(Arrays.asList("developoffi@gmail.com"))
                .subject("test email")
                .content("test content")
                .build()
        );
        */

        /*
        String subject = "쇼핑몰 웹사이트 회원가입 계정인증 이메일입니다.";
        String content = "<h3>이메일 인증을 위하여 {{ username }} 계정에 발송된 인증메일입니다. "
                + "하단의 링크를 클릭해서 인증을 완료해주세요.</h3>"
                + "<a href='{{ webUrl }}/seller/certified?user={{ userId }}"
                + "&token={{ token }}"
                + "'>여기를 클릭해주세요!</a>";
        senderService.updateTemplate(JOIN_MAIL_TEMPLATE, subject,null, content);
        */

        Map<String, String> templateData = new HashMap<>();
        templateData.put("username", "test name");
        templateData.put("webUrl", webUrl);
        templateData.put("userId", "test id");
        templateData.put("token", "test token");
        senderService.sendTemplatedMail(SenderDto.SenderTemplateDto(
                MAIL_ADDRESS, Arrays.asList(MAIL_ADDRESS),
                JOIN_MAIL_TEMPLATE, templateData)
        );
    }

}
