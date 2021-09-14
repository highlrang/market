package com.myproject.myweb.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity // (debug = true)
@Configuration
public class SecurityConfig {

    public static final String[] SECURITY_PASSED_URLS = {
            "/", "/js/**", "/css/**",
            "/customer/join", "/customer/certify", "/customer/certified", "/customer/login",
            "/seller/join", "/seller/certify", "/seller/certified", "/seller/login"
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
