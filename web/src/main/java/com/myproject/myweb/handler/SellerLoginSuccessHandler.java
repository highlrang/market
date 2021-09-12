package com.myproject.myweb.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SellerLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        HttpSession session = request.getSession();
        System.out.println(authentication.getPrincipal());

        session.setAttribute("seller", authentication.getPrincipal());

        try {
            response.sendRedirect("/");
        } catch (IOException ignored) { }
    }

}
