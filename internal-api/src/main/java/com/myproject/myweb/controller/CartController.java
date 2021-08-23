package com.myproject.myweb.controller;

import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/save")
    public String save(
                     @RequestParam(value = "user_id") Long userId, // session값 받기
                     @RequestParam(value = "item_id") Long itemId,
                     @RequestParam(value = "count") int count,
                     HttpServletRequest request){
        cartService.put(userId, itemId, count);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/order")
    public String order(@RequestParam(value = "item_id") List<Long> itemIds,
                        @RequestParam(value = "user_id") Long userId){
        Long orderId = cartService.toOrder(userId, itemIds);

        // 복수건 결제 로직 만들기 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return "order/detail";
    }
}
