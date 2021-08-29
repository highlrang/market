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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final WebClient webClient = WebClient.create();
    private final OrderService orderService;
    private final CartService cartService;
    private final MessageSource messageSource;
    private final static String cid = "TC0ONETIME";


    // 익셉션 처리 + HttpStatus 200 이외의 경우 처리
    @PostMapping("/payment/ready")
    public String readyPayment(@RequestParam(value = "user_id") Long userId,
                               @RequestParam(value = "item_id") List<Long> itemIds,
                               @RequestParam(value = "count", required = false) String count,
                               @RequestParam(value = "coupon") String couponId,
                               @RequestParam(value = "cart_id", required = false) String cartId){

        Boolean orderImpossible = orderService.orderImpossible(userId);
        if(orderImpossible) {
            orderRedirectAttributes("주문 중인 상품이 있어 진행이 불가능합니다."); // template에 추가
            return "redirect:/";
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

        OrderResponseDto orderResponseDto = orderService.findById(orderId);

        String myHost = "http://127.0.0.1:8081/order";
        String kakaopayUrl = "https://kapi.kakao.com/v1/payment/ready";

        MultiValueMap<String, String> parameterMap = getParameterMap(userId, orderId);
        parameterMap.add("item_name", orderResponseDto.getOrderItemsName());
        parameterMap.add("quantity", String.valueOf(orderResponseDto.getTotalCount()));
        parameterMap.add("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
        parameterMap.add("tax_free_amount", "0");

        parameterMap.add("approval_url", myHost + "/payment/approve");
        parameterMap.add("cancel_url", myHost + "/cancel?orderId=" + orderId);
        parameterMap.add("fail_url", myHost + "/fail?orderId=" + orderId);

        try {
            Mono<PaymentReadyDto> response = webClient
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
                    .retrieve()
                    .bodyToMono(PaymentReadyDto.class);

            PaymentReadyDto paymentReadyDto = response.block();
            orderService.saveTid(orderId, paymentReadyDto.getTid());

            return "redirect:" + paymentReadyDto.getNext_redirect_pc_url();

        }catch (Exception e){
            log.error(e.getMessage());
            orderService.remove(orderId);
            return "redirect:/"; // 수정
        }
    }


    private MultiValueMap<String, String> getParameterMap(@RequestParam("user_id") Long userId, Long orderId) {
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", cid);
        parameterMap.add("partner_order_id", String.valueOf(orderId));
        parameterMap.add("partner_user_id", String.valueOf(userId));
        return parameterMap;
    }

    @RequestMapping("/cancel")
    public String orderCancel(@RequestParam(value = "orderId") Long orderId){
        orderService.cancel(orderId);
        orderRedirectAttributes("결제를 취소했습니다.");
        return "redirect:/";
        // 하나일 경우 상품 상세 페이지(itemId받아서) 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/fail")
    public String orderFail(@RequestParam(value = "orderId") Long orderId){
        orderService.cancel(orderId); // 취소가 아닌 실패 메서드 따로 만들기
        orderRedirectAttributes("결제 실패했습니다.");
        return "redirect:/";
    }

    @RequestMapping("/payment/approve") //GET
    public String approvePayment(@RequestParam(value = "pg_token") String pg_token, HttpSession session){

        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        OrderResponseDto order = orderService.findOrderReady(user.getId());

        Long cartId = 0L; List<Long> itemIds = new ArrayList<>();
        try {
            cartId = cartService.findByUser(user.getId()).getId();
            itemIds = order.getOrderItems().stream()
                    .map(OrderItemDto::getItemId)
                    .collect(Collectors.toList());

        } catch(IllegalArgumentException ignored){ }

        MultiValueMap<String, String> parameterMap = getParameterMap(user.getId(), order.getId());
        parameterMap.add("tid", order.getTid());
        parameterMap.add("pg_token", pg_token);

        String baseUrl = "https://kapi.kakao.com/v1/payment/approve";

        ResponseEntity<Object> response = webClient.mutate()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeaders.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                })
                .build()
                .post()
                .body(BodyInserters.fromFormData(parameterMap))
                .retrieve()
                .toEntity(Object.class)
                .block();

        if(response.getStatusCode().equals(HttpStatus.OK)) {
            orderService.updateOrderStatus(order.getId(), OrderStatus.COMP);
            if(cartId != 0L) {
                cartService.remove(cartId, itemIds); // 쿠폰까지 삭제됨
            }

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

    @GetMapping("/payment/cancel/{orderId}")
    public String paymentCancel(@PathVariable("orderId") Long orderId){

        OrderResponseDto order = orderService.findById(orderId);
        if(!order.getOrderStatus().equals("주문 완료")) throw new IllegalStateException("PaymentCancelFailed"); //

        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", cid);
        parameterMap.add("tid", order.getTid());
        parameterMap.add("cancel_amount", String.valueOf(order.getTotalPrice()));
        parameterMap.add("cancel_tax_free_amount", "0");

        Mono<ResponseEntity<Object>> mono = webClient.mutate()
                .baseUrl("https://kapi.kakao.com/v1/payment/cancel")
                .defaultHeaders(
                        httpHeaders -> {
                            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            httpHeaders.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                        }
                )
                .build()
                .post()
                .body(BodyInserters.fromFormData(parameterMap))
                .retrieve()
                .toEntity(Object.class);

        try{
            ResponseEntity<Object> response = mono.block();
            if(response.getStatusCode().is2xxSuccessful()) orderService.cancel(orderId);

        }catch(Exception e){
            log.error("결제 취소가 실패했습니다. -> " + e.getMessage());
            RedirectAttributes attributes = new RedirectAttributesModelMap();
            attributes.addAttribute("msg", "paymentCancelFailed");
        }

        return "redirect:/order/list";
    }

}
