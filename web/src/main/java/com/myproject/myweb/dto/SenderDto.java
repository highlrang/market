package com.myproject.myweb.dto;

import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@NoArgsConstructor
public class SenderDto {
    private String from;
    private List<String> to = new ArrayList<>();
    private String template_name;
    private Map<String, String> template_data = new HashMap<>();
    private String subject;
    private String content;

    @Builder
    public SenderDto(String from, List<String> to, String subject, String content){
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    public static SenderDto SenderTemplateDto(String from, List<String> to, String template_name, Map<String, String> template_data){
        SenderDto dto = new SenderDto();
        dto.from = from;
        dto.to = to;
        dto.template_name = template_name;
        dto.template_data = template_data;
        return dto;
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

    public SendTemplatedEmailRequest toSendTemplatedRequestDto() {
        Destination destination = new Destination().withToAddresses(this.to);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStringData = null;
        try{
            jsonStringData = objectMapper.writeValueAsString(this.template_data);
        }catch (JsonProcessingException e){
            log.error("Map to String converter error > " + e.getMessage());
        }

        return new SendTemplatedEmailRequest()
                .withSource(this.from)
                .withDestination(destination)
                .withTemplate(this.template_name)
                .withTemplateData(jsonStringData);
    }


}
