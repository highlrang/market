package com.myproject.myweb.controller;

import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpSession;
import java.util.*;
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
        String url;
        if(cartId == null) {
            url = "redirect:/item/detail/" + itemIds.get(0);
        }else{
            url = "redirect:/cart/detail/" + cartId;
        }

        if(orderImpossible) {
            orderRedirectAttributes("OrderAlreadyInProgress");
            return url;
            // 주문 페이지 따로 생기기 전까지는 item detail 또는 cart detail 페이지로
        }

        Long orderId;
        try {
            if (cartId != null) {
                // 장바구니에서 주문 (단건, 복수건) >> user exception >> common handler home redirect
                orderId = cartService.order(userId, itemIds);
            } else {
                // 상품 단건 바로 주문 >> user or item exception >> common handler home redirect
                orderId = orderService.order(userId, itemIds.get(0), Integer.parseInt(count), couponId); // 여러 상품들 >> 하나의 주문서 생성
            }
        }catch (ItemStockException e){
            String msg = messageSource.getMessage(e.getMessage(), e.getArgs(), Locale.getDefault());
            log.error("item id = " + e.getArgs()[0] + " " + msg);
            orderRedirectAttributes(msg);
            return url;
        }

        try {
            String finalUrl = paymentService.ready(userId, orderId);
            if (finalUrl != null) return "redirect:" + finalUrl;

        }catch (WebClientResponseException e){
            log.error("status code = " + e.getRawStatusCode() + " " + e.getMessage() + " >> 결제 요청 실패로 주문 삭제");
            orderService.remove(orderId);
        }

        return url;
    }

    @RequestMapping("/payment/cancel")
    public String paymentCancel(@RequestParam(value = "orderId") Long orderId){
        // status QUIT_PAYMENT 확인하기
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId);
        orderService.remove(orderId); // 결제 중 취소이기에 배달 완료 익셉션 NO
        orderRedirectAttributes("PaymentCancel");
        return "redirect:/" + url;
        // 일단 item 하나일 경우 상품 상세 페이지, 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/payment/fail")
    public String orderFail(@RequestParam(value = "orderId") Long orderId){
        // status QUIT_PAYMENT 확인하기
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId);
        orderService.remove(orderId); // 결제 중 취소, 배달 완료 NO
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

        try {
            paymentService.approve(user.getId(), order.getId(), pg_token);

            orderService.updateOrderStatus(order.getId(), OrderStatus.COMP); // 결제완료된 orderId exception은 없을 가능성이 높아 common handler로만 처리
            if (cartId != 0L) {
                cartService.remove(cartId, itemIds);
            }
            return "redirect:/order/detail/" + order.getId();

        }catch (WebClientResponseException e) {
            // 400 Bad Request Error etc...
            orderRedirectAttributes("PaymentFailed");
            if (cartId != 0L) return "redirect:/cart/detail/" + cartId;
            return "redirect:/item/detail/" + order.getOrderItems().get(0).getId();
        }
    }

    @RequestMapping("/cancel/{orderId}")
    public String orderCancel(@RequestParam(value = "orderId") Long orderId){
        String msg = "";
        try{
            msg = paymentService.cancel(orderId); // orderService.cancel() 까지 호출, exception은 common으로 해결
        }catch (IllegalStateException e){
            if(e.getMessage().equals("DeliveryAlreadyCompletedException")) { // 배달완료 exception 처리
                msg = e.getMessage();
            }
            log.error(e.getMessage());
        }catch (WebClientResponseException e){
            log.error("status code = " + e.getRawStatusCode() + " " + e.getMessage() + " 결제 취소 실패");
            msg = "PaymentCancelFailed";
        }
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
        OrderResponseDto order = orderService.findById(id); // list에서만 호출가능한 것이 아니라 common exception handler로
        model.addAttribute("order", order);
        return "order/detail";
    }

}
