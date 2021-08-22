package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.order.PaymentReadyDto;
import com.myproject.myweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final WebClient webClient;
    private final OrderService orderService;


    // 익셉션 처리 + HttpStatus 200 이외의 경우 처리

    @GetMapping("/payment/ready") // 단건 결제 >> 장바구니 복수건도 해보기
    public String readyPayment(@RequestParam(value = "user_id") Long userId, // form?
                               @RequestParam(value = "item_id") Long itemId,
                               @RequestParam(value = "count") int count){

        Long orderId = orderService.order(userId, itemId, count);
        OrderResponseDto orderResponseDto = orderService.findById(orderId);

        String myHost = "http://127.0.0.1:8081/order";
        String baseUrl = "https://kapi.kakao.com/v1/payment/ready";

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        header.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("cid", "TC0ONETIME");
        parameterMap.put("partner_order_id", String.valueOf(orderId));
        parameterMap.put("partner_user_id", String.valueOf(userId));

        List<OrderItemDto> orderItemDtos = orderResponseDto.getOrderItems();
        String itemName = orderItemDtos.get(0).getName();
        if(orderItemDtos.size() > 1) itemName += " 외 " + orderItemDtos.size() + " 종류의 상품";
        parameterMap.put("item_name", itemName);

        parameterMap.put("quantity", String.valueOf(orderResponseDto.getTotalCount()));
        parameterMap.put("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
        parameterMap.put("tax_free_amount", "0");
        parameterMap.put("approval_url", myHost + "/payment/approve");
        parameterMap.put("cancel_url", myHost + "/cancel"); cancel, fail 만들기
        parameterMap.put("fail_url", myHost + "/fail");

        Mono<PaymentReadyDto> mono = webClient
                .mutate()
                .baseUrl(baseUrl)
                // .defaultHeader("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2")
                // .defaultHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .defaultHeaders((Consumer<HttpHeaders>) header)
                .build()
                .post()
                .attributes((Consumer<Map<String, Object>>) parameterMap)
                .retrieve()
                .bodyToMono(PaymentReadyDto.class);

        PaymentReadyDto paymentReadyDto = mono.block();
        paymentReadyDto.getTid(); 저장하기

        return "redirect:" + paymentReadyDto.getNext_redirect_pc_url();
    }

    @RequestMapping("/payment/approve")
    public String approvePayment(@RequestParam(value = "pg") String pg_token, HttpSession session){

        // session user로 진행 중인 order id 가져오기?
        UserResponseDto user = (UserResponseDto) session.getAttribute("user"); // login시 session 저장하기

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        header.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("cid", "TC0ONETIME");
        parameterMap.put("tid", );
        parameterMap.put("partner_order_id", );
        parameterMap.put("partner_user_id", user.getId());
        parameterMap.put("pg_token", pg_token);

        String baseUrl = "https://kapi.kakao.com/v1/payment/approve";

        webClient.mutate()
                .baseUrl(baseUrl)
                .defaultHeaders((Consumer<HttpHeaders>) header)
                .build()
                .post()
                .attributes((Consumer<Map<String, Object>>) parameterMap)
                .retrieve();

        return "";
    }

}
