package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.order.PaymentReadyDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final WebClient webClient = WebClient.create();
    private final OrderService orderService;
    private final static String cid = "TC0ONETIME";


    // 익셉션 처리 + HttpStatus 200 이외의 경우 처리
    // 단건 결제 >> 장바구니 복수건도 해보기
    @PostMapping("/payment/ready")
    public String readyPayment(HttpServletRequest request,
                               @RequestParam(value = "user_id") Long userId,
                               @RequestParam(value = "item_id") Long itemId,
                               @RequestParam(value = "count") int count){

        // error 발생 시 orderService.remove(orderId);
        orderService.remove(4L);
        orderService.remove(5L);

        Boolean orderImpossible = orderService.orderImpossible(userId);
        if(orderImpossible) {
            orderRedirectAttributes("주문 중인 상품이 있습니다."); // template에 추가
            return "redirect:" + request.getHeader("Referer");
        }

        Long orderId = orderService.order(userId, itemId, count);
        OrderResponseDto orderResponseDto = orderService.findById(orderId);

        String myHost = "http://127.0.0.1:8081/order";
        String kakaopayUrl = "https://kapi.kakao.com/v1/payment/ready";

        MultiValueMap<String, String> parameterMap = getParameterMap(userId, orderId);
        parameterMap.add("item_name", orderResponseDto.getOrderItemsName());
        parameterMap.add("quantity", String.valueOf(orderResponseDto.getTotalCount()));
        parameterMap.add("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
        parameterMap.add("tax_free_amount", "0");

        parameterMap.add("approval_url", myHost + "/payment/approve");
        parameterMap.add("cancel_url", myHost + "/cancel?id=" + itemId);
        parameterMap.add("fail_url", myHost + "/fail?id=" + itemId);

        WebClient.ResponseSpec response = webClient
                .mutate()
                .baseUrl(kakaopayUrl)
                // "application/x-www-form-urlencoded;charset=utf-8"
                .defaultHeaders(httpHeader -> {
                    httpHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeader.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                })
                .build()
                .post()
                .body(BodyInserters.fromFormData(parameterMap))
                .retrieve();

        ResponseEntity<PaymentReadyDto> responseEntity = response.toEntity(PaymentReadyDto.class).block();
        log.info(String.valueOf(responseEntity.getStatusCodeValue()));
        PaymentReadyDto paymentReadyDto = responseEntity.getBody();

        log.info("url 전달 " + paymentReadyDto.getNext_redirect_pc_url());
        orderService.saveTid(orderId, paymentReadyDto.getTid());

        return "redirect:" + paymentReadyDto.getNext_redirect_pc_url();
    }



    private MultiValueMap<String, String> getParameterMap(@RequestParam("user_id") Long userId, Long orderId) {
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", cid);
        parameterMap.add("partner_order_id", String.valueOf(orderId));
        parameterMap.add("partner_user_id", String.valueOf(userId));
        return parameterMap;
    }

    @RequestMapping("/cancel")
    public String orderCancel(@RequestParam(value = "id") Long id){
        orderRedirectAttributes("결제를 취소했습니다.");
        return "redirect:/item/detail" + id;
    }

    @RequestMapping("/fail")
    public String orderFail(@RequestParam(value = "id") Long id){
        orderRedirectAttributes("결제 실패했습니다.");
        return "redirect:/item/detail" + id;
    }

    @RequestMapping("/payment/approve")
    public String approvePayment(@RequestParam(value = "pg") String pg_token, HttpSession session){

        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        OrderResponseDto order = orderService.findOrderReady(user.getId());

        MultiValueMap<String, String> parameterMap = getParameterMap(user.getId(), order.getId());
        parameterMap.add("tid", order.getTid());
        parameterMap.add("pg_token", pg_token);

        String baseUrl = "https://kapi.kakao.com/v1/payment/approve";

        Mono<ResponseEntity> response = webClient.mutate()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeaders.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                })
                .build()
                .post()
                .body(BodyInserters.fromFormData(parameterMap))
                .retrieve()
                .bodyToMono(ResponseEntity.class);

        ResponseEntity entity = response.block();
        if(entity.getStatusCode().equals(HttpStatus.OK)) {
            orderService.updateOrderStatus(order.getId(), OrderStatus.COMP);
        }else{
            // 장바구니에서는 장바구니로 돌아가기
            orderRedirectAttributes("결제 실패했습니다.");
            return "redirect:/item/detail/" + order.getOrderItems().get(0).getId();
            // 보통은 결제 전 페이지, 나는 일단 상품 디테일 페이지
        }

        return "redirect:/order/detail/" + order.getId();
    }

    private void orderRedirectAttributes(String msg) {
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        attributes.addAttribute("orderFail", msg);
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model){
        OrderResponseDto order = orderService.findById(id);
        model.addAttribute("order", order);
        return "order/detail";
    }

}
