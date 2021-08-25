package com.myproject.myweb.controller;

import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;

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
                     @RequestParam(value = "user_id") Long userId,
                     @RequestParam(value = "item_id") Long itemId,
                     @RequestParam(value = "count") int count,
                     RedirectAttributes attributes){
        cartService.put(userId, itemId, count);
        attributes.addAttribute("msg", "CartSave");
        return "redirect:/item/detail/" + itemId;
    }

    @PostMapping("/remove") // 장바구니에서만 호출
    public String remove(
            @RequestParam("cart_id") Long cartId,
            @RequestParam("item_id") List<Long> itemIds){
        cartService.remove(cartId, itemIds);
        return "redirect:/cart/detail/" + cartId;
    }
}
