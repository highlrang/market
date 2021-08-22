package com.myproject.myweb.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter @NoArgsConstructor
public class PaymentReadyDto {

    private String tid;
    private String next_redirect_app_url;
    private String next_redirect_mobile_url;
    private String next_redirect_pc_url;
    private String android_app_scheme;
    private String ios_app_scheme;
    private LocalDateTime created_at;

    public void setCreated_at(String created_at){
        LocalDateTime dateTime = LocalDateTime.parse(created_at, DateTimeFormatter.ISO_DATE_TIME);
        this.created_at = dateTime;
    }
}
