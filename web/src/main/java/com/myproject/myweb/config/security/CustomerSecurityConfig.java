package com.myproject.myweb.config.security;

import com.myproject.myweb.service.user.CustomerService;
import com.myproject.myweb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
public class CustomerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomerService customerService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers("/js/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http
                .authorizeRequests()
                    .antMatchers("/", "/join", "/certify", "/certified", "/login")
                    .permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successForwardUrl("/")
                    .permitAll()
                    .and()
                .logout()
                    .logoutUrl("/logout")
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
