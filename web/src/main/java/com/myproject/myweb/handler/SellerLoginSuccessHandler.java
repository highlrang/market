package com.myproject.myweb.handler;

import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.service.SellerNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RequiredArgsConstructor
public class SellerLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SellerNoticeService noticeService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        HttpSession session = request.getSession();
        SellerResponseDto seller = (SellerResponseDto) authentication.getPrincipal();
        session.setAttribute("seller", seller);

        session.setAttribute("unreadNotice", noticeService.countUnreadBySeller(seller.getId()));
        try {
            response.sendRedirect("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
