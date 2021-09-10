package com.myproject.myweb.config.security;

import com.myproject.myweb.service.user.SellerService;
import com.myproject.myweb.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
public class SellerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SellerService sellerService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers("/js/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http
                .authorizeRequests()
                    .antMatchers("/", "/seller/join", "/seller/certify", "/seller/certified", "/seller/login")
                    .permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/seller/login")
                    .loginProcessingUrl("/seller/login")
                    .successForwardUrl("/")
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
