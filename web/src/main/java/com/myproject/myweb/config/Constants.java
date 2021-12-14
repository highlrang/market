package com.myproject.myweb.config;

import org.springframework.beans.factory.annotation.Value;

public final class Constants {

    public static String WEB_URL;
    @Value("${webUrl}")
    public void setWebUrl(String webUrl){
        this.WEB_URL = webUrl;
    }
    public static final String MAIL_ADDRESS = "developoffi@gmail.com";
    public static final String KAKAOPAY_CID = "TC0ONETIME";

    public static final String JOIN_MAIL_TEMPLATE = "JoinCertificationMailTemplate";
}
