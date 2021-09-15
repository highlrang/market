package com.myproject.myweb.config.security;

import com.myproject.myweb.handler.CustomerLoginSuccessHandler;
import com.myproject.myweb.service.user.CustomerService;
import com.myproject.myweb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.myproject.myweb.config.security.SecurityConfig.SECURITY_PASSED_URLS;

@Order(1)
@Configuration
@RequiredArgsConstructor
public class CustomerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomerService customerService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http
                .requestMatchers()
                    .antMatchers("/customer/**")
                    .and()
                .authorizeRequests()
                    .antMatchers(SECURITY_PASSED_URLS).permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/customer/login")
                    .loginProcessingUrl("/customer/login")
                    .failureUrl("/customer/login?msg=LoginError")
                    .successHandler(new CustomerLoginSuccessHandler())
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/customer/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
                    .and()
                .csrf()
                    .disable();

    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(customerService).passwordEncoder(passwordEncoder);
    }

}
