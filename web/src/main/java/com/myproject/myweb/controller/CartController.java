package com.myproject.myweb.controller;

import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.CouponService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
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
    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;
    private final CouponService couponService;
    private final MessageSource messageSource;

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable(value = "id") Long id,
                         @RequestParam(value = "msg", required = false) String msg,
                         HttpSession session,
                         Model model){
        CartResponseDto cart = cartService.findById(id);
        model.addAttribute("cart", cart);

        CustomerResponseDto customer = (CustomerResponseDto) session.getAttribute("customer");
        model.addAttribute("coupons", couponService.findAvailableCouponByCustomer(customer.getId()));

        if(msg != null) model.addAttribute("msg", messageSource.getMessage("msg", null, Locale.getDefault()));
        return "cart/detail";
    }

    @PostMapping("/save")
    public String save(
                     @RequestParam(value = "customer_id") Long customerId,
                     @RequestParam(value = "item_id") Long itemId,
                     @RequestParam(value = "count") int count,
                     @RequestParam(value = "coupon", required = false) String couponId,
                     RedirectAttributes attributes){

        String msg;
        try {
            cartService.put(customerId, itemId, count, couponId);
            msg = "CartSave";

        }catch(IllegalArgumentException | IllegalStateException e){
            // msg = "CartSaveFail";
            msg = e.getMessage();
        }

        attributes.addAttribute("msg", msg);
        return "redirect:/item/detail/" + itemId;
    }

    @PostMapping("/update")
    public String update(
            @RequestParam(value = "cart_id") Long cartId,
            @RequestParam(value = "cartItem_id") Long cartItemId,
            @RequestParam(value = "count") int count,
            @RequestParam(value = "coupon", required = false) String couponId,
            RedirectAttributes attributes){
        try {
            cartService.update(cartItemId, count, couponId);

        }catch (IllegalArgumentException e){
            attributes.addAttribute("msg", e.getMessage());
        }

        // Long cartId = cartService.findCartIdByCartItemId(cartItemId);
        // 쿼리 줄이기 위해 변수로 가져옴

        return "redirect:/cart/detail/" + cartId;
    }


    @PostMapping("/remove") // 장바구니에서만 호출
    public String remove(
            @RequestParam("cart_id") Long cartId,
            @RequestParam("delete_item_id") List<Long> itemIds,
            RedirectAttributes attributes){
        try {
            cartService.remove(cartId, itemIds);

        }catch (IllegalArgumentException e){
            attributes.addAttribute("msg", e.getMessage());
        }

        return "redirect:/cart/detail/" + cartId;
    }

    @PostMapping("/order/ready")
    public String orderReady(@RequestParam(value = "customer_id") Long customerId,
                             @RequestParam(value = "item_id") List<Long> itemIds,
                             @RequestParam(value = "cart_id") String cartId,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes){

        Boolean orderPresent = orderService.orderImpossible(customerId);
        if(orderPresent) orderService.removeOrderStatusReady(customerId);

        try {
            Long orderId = cartService.order(customerId, itemIds);
            session.setAttribute("order", "cart");
            model.addAttribute("order", orderService.findById(orderId));
            return "order/create";

        }catch (ItemStockException e){
            String msg = messageSource.getMessage(e.getMessage(), new String[]{e.getArgs()[1]}, Locale.getDefault());
            itemService.stockNotice(Long.valueOf(e.getArgs()[0]));
            redirectAttributes.addAttribute("msg", msg);
            return "redirect:/cart/detail/" + cartId;
        }
    }
}
