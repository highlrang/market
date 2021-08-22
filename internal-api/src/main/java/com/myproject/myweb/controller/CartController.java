package com.myproject.myweb.controller;

import com.myproject.myweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/save")
    public void save(){ // restcontroller로 해서 완료 후 현재 페이지 머물르기 or 장바구니 페이지로 이동하기
        cartService.put();
    }

    @PostMapping("/order")
    public String order(){ // userId랑 item ids 받기
        cartService.toOrder();
    }
}
