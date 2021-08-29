package com.myproject.myweb.controller;

import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable(value = "id") Long id, HttpSession session, Model model){
        CartResponseDto cart = cartService.findById(id);
        model.addAttribute("cart", cart);

        // cartItem에 사용되지 않은 쿠폰만 보내기
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        model.addAttribute("coupons", couponService.findByUserAndCanUse(user.getId()));
        return "cart/detail";
    }

    @PostMapping("/save")
    public String save(
                     @RequestParam(value = "user_id") Long userId,
                     @RequestParam(value = "item_id") Long itemId,
                     @RequestParam(value = "count") int count,
                     @RequestParam(value = "coupon", required = false) String couponId,
                     RedirectAttributes attributes){

        cartService.put(userId, itemId, count, couponId);
        attributes.addAttribute("msg", "CartSave");
        return "redirect:/item/detail/" + itemId;
    }

    @PostMapping("/update")
    public String update(
            @RequestParam(value = "cartItem_id") Long cartItemId,
            @RequestParam(value = "count") int count,
            @RequestParam(value = "coupon", required = false) String couponId){
        cartService.update(cartItemId, count, couponId);
        Long cartId = cartService.findCartIdByCartItemId(cartItemId);
        return "redirect:/cart/detail/" + cartId;
    }


    @PostMapping("/remove") // 장바구니에서만 호출
    public String remove(
            @RequestParam("cart_id") Long cartId,
            @RequestParam("item_id") List<Long> itemIds){
        cartService.remove(cartId, itemIds);
        return "redirect:/cart/detail/" + cartId;
    }
}
