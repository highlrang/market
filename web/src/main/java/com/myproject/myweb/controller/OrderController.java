package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.OrderItem;
import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.order.PaymentReadyDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final MessageSource messageSource;


    @PostMapping("/payment/ready")
    public String readyPayment(@RequestParam(value = "user_id") Long userId,
                               @RequestParam(value = "item_id") List<Long> itemIds,
                               @RequestParam(value = "count", required = false) String count,
                               @RequestParam(value = "coupon") String couponId,
                               @RequestParam(value = "cart_id", required = false) String cartId){

        Boolean orderImpossible = orderService.orderImpossible(userId);
        if(orderImpossible) {
            orderRedirectAttributes("OrderAlreadyInProgress");
            if(cartId == null) return "redirect:/item/detail/" + itemIds.get(0);
            return "redirect:/cart/detail/" + cartId;
            // 주문 페이지 따로 생기기 전까지는 item detail 또는 cart detail 페이지로
        }

        Long orderId;
        if(cartId != null){
            // 장바구니에서 주문 (단건, 복수건)
            orderId = cartService.order(userId, itemIds);
        }else {
            // 상품 단건 바로 주문
            orderId = orderService.order(userId, itemIds.get(0), Integer.parseInt(count), couponId); // 여러 상품들 >> 하나의 주문서 생성
            // stock zero exception 대응하기
        }

        String url = paymentService.ready(userId, orderId);
        if(url != null) return "redirect:" + url;

        if(cartId == null) return "redirect:/item/detail" + itemIds.get(0);
        return "redirect:/cart/detail" + cartId;
    }

    @RequestMapping("/payment/cancel")
    public String paymentCancel(@RequestParam(value = "orderId") Long orderId){
        // status QUIT_PAYMENT 확인하기
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId);
        orderService.remove(orderId);
        orderRedirectAttributes("PaymentCancel");
        return "redirect:/" + url;
        // 일단 item 하나일 경우 상품 상세 페이지, 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/payment/fail")
    public String orderFail(@RequestParam(value = "orderId") Long orderId){
        // status QUIT_PAYMENT 확인하기
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId);
        orderService.remove(orderId);
        orderRedirectAttributes("PaymentFailed");
        return "redirect:/" + url;
        // 일단 item 하나일 경우 상품 상세 페이지, 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/payment/approve") //GET
    public String approvePayment(@RequestParam(value = "pg_token") String pg_token, HttpSession session){

        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        OrderResponseDto order = orderService.findOrderReady(user.getId());

        List<Long> itemIds = order.getOrderItems().stream()
                .map(OrderItemDto::getItemId)
                .collect(Collectors.toList());

        Long cartId = 0L;
        try {
            cartId = cartService.findByUser(user.getId()).getId();
        } catch(IllegalArgumentException ignored){ }

        Boolean success = paymentService.approve(user.getId(), order.getId(), pg_token);
        if(success) {
            orderService.updateOrderStatus(order.getId(), OrderStatus.COMP);
            if (cartId != 0L) {
                cartService.remove(cartId, itemIds); // 쿠폰까지 삭제됨
            }
            return "redirect:/order/detail/" + order.getId();
        }

        // 400 Bad Request Error etc...
        orderRedirectAttributes("PaymentFailed");
        if(cartId != 0L) return "redirect:/cart/detail/" + cartId;
        return "redirect:/item/detail/" + order.getOrderItems().get(0).getId();

    }

    @RequestMapping("/cancel/{orderId}")
    public String orderCancel(@RequestParam(value = "orderId") Long orderId){
        String msg = paymentService.cancel(orderId);// orderService.cancel() 까지 호출
        orderRedirectAttributes(msg);
        return "redirect:/order/list";
    }

    private void orderRedirectAttributes(String msg) {
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        attributes.addAttribute("msg", msg);
    }

    @GetMapping("/list")
    public String list(@RequestParam(value = "msg", required = false) String msg,
                       HttpSession session, Model model){
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        List<OrderResponseDto> orders = orderService.findByUserId(user.getId());
        model.addAttribute("orders", orders);
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "order/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderResponseDto order = orderService.findById(id);
        model.addAttribute("order", order);
        return "order/detail";
    }

}
