package com.myproject.myweb.config.security;

import com.myproject.myweb.handler.SellerLoginSuccessHandler;
import com.myproject.myweb.service.user.SellerService;
import com.myproject.myweb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.myproject.myweb.config.security.SecurityConfig.SECURITY_PASSED_URLS;

@Order(2)
@Configuration
@RequiredArgsConstructor
public class SellerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SellerService sellerService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http
                .requestMatchers()
                    .antMatchers("/seller/**")
                    .and()
                .authorizeRequests()
                    .antMatchers(SECURITY_PASSED_URLS).permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/seller/login")
                    .loginProcessingUrl("/seller/login")
                    .failureUrl("/seller/login?msg=LoginError")
                    .successHandler(new SellerLoginSuccessHandler())
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/seller/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
                    .and()
                .csrf()
                    .disable();

    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(sellerService).passwordEncoder(passwordEncoder);
    }
}
