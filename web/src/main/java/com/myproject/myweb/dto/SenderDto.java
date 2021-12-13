package com.myproject.myweb.dto;

import com.amazonaws.services.simpleemail.model.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SenderDto {
    public static String from;
    @Value("${gmail.username}")
    public void setFrom(String from){
        this.from = from;
    }
    private List<String> to = new ArrayList<>();
    private String subject;
    private String content;

    @Builder
    public SenderDto(List<String> to, String subject, String content){
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public void addTo(String email){
        this.to.add(email);
    }

    public SendEmailRequest toSendRequestDto(){
        Destination destination = new Destination().withToAddresses(this.to);

        Message message = new Message()
                .withSubject(createContent(this.subject))
                .withBody(new Body()
                        .withHtml(createContent(this.content))
                );

        return new SendEmailRequest()
                .withSource(this.from)
                .withDestination(destination)
                .withMessage(message);
    }

    private Content createContent(String text){
        return new Content()
                .withCharset("UTF-8")
                .withData(text);
    }


}
