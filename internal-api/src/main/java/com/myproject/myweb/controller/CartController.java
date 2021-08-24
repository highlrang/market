package com.myproject.myweb.controller;

import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable(value = "id") Long id, Model model){
        CartResponseDto cart = cartService.findById(id);
        model.addAttribute("cart", cart);
        return "cart/detail";
    }

    @PostMapping("/save")
    public String save(
                     @RequestParam(value = "user_id") Long userId, // session값 받기
                     @RequestParam(value = "item_id") Long itemId,
                     @RequestParam(value = "count") int count,
                     HttpServletRequest request){
        cartService.put(userId, itemId, count);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/remove") // 장바구니에서만 호출
    public String remove(
            @RequestParam("cart_id") Long cartId,
            @RequestParam("item_id") List<Long> itemIds){
        cartService.remove(cartId, itemIds);
        return "redirect:/cart/detail/" + cartId;
    }
}
