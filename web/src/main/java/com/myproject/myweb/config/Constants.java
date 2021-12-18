package com.myproject.myweb.config;

import com.amazonaws.regions.Regions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String MAIL_ADDRESS = "developoffi@gmail.com";
    public static final String KAKAOPAY_CID = "TC0ONETIME";
    public static final String JOIN_MAIL_TEMPLATE = "JoinCertificationMailTemplate";
    public static final String AWS_REGION = Regions.AP_NORTHEAST_2.getName();
}
