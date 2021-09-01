package com.myproject.myweb.service;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.order.PaymentReadyDto;
import com.myproject.myweb.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final WebClient webClient = WebClient.create();
    @Value("${kakaopay.cid}")
    private final String cid;

    private MultiValueMap<String, String> getParameterMap(@RequestParam("user_id") Long userId, Long orderId) {
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", cid);
        parameterMap.add("partner_order_id", String.valueOf(orderId));
        parameterMap.add("partner_user_id", String.valueOf(userId));
        return parameterMap;
    }

    @Transactional
    public String ready(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        OrderResponseDto orderResponseDto = new OrderResponseDto(order);

        String myHost = "http://127.0.0.1:8081/order/payment";
        String kakaopayUrl = "https://kapi.kakao.com/v1/payment/ready";

        MultiValueMap<String, String> parameterMap = getParameterMap(userId, orderId);
        parameterMap.add("item_name", orderResponseDto.getOrderItemsName());
        parameterMap.add("quantity", String.valueOf(orderResponseDto.getTotalCount()));
        parameterMap.add("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
        parameterMap.add("tax_free_amount", "0");

        parameterMap.add("approval_url", myHost + "/approve");
        parameterMap.add("cancel_url", myHost + "/cancel?orderId=" + orderId);
        parameterMap.add("fail_url", myHost + "/fail?orderId=" + orderId);

        try {
            Mono<ResponseEntity<PaymentReadyDto>> mono = webClient
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
                    .toEntity(PaymentReadyDto.class);

            ResponseEntity<PaymentReadyDto> responseEntity = mono.block();
            if(responseEntity.getStatusCode().is2xxSuccessful()) {
                PaymentReadyDto dto = responseEntity.getBody();
                this.saveTid(orderId, dto.getTid());
                return dto.getNext_redirect_pc_url();
            }
            log.error(responseEntity.getStatusCodeValue() + " 결제 요청 실패");
            orderService.remove(orderId);
            return null;

        }catch (Exception e){
            log.error(e.getMessage());
            orderService.remove(orderId);
            return null;
        }
    }

    @Transactional
    public void saveTid(Long orderId, String tid){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.setTid(tid);
    }

    public Boolean approve(Long userId, Long orderId, String pg_token){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));

        MultiValueMap<String, String> parameterMap = getParameterMap(userId, orderId);
        parameterMap.add("tid", order.getTid());
        parameterMap.add("pg_token", pg_token);

        String baseUrl = "https://kapi.kakao.com/v1/payment/approve";

        try {
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

            return response.getStatusCode().is2xxSuccessful();

        }catch(Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    @Transactional
    public String cancel(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));

        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", cid);
        parameterMap.add("tid", order.getTid());
        parameterMap.add("cancel_amount", String.valueOf(order.getTotalPrice()));
        parameterMap.add("cancel_tax_free_amount", "0");

        String msg;
        try{
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

            ResponseEntity<Object> response = mono.block();

            if(response.getStatusCode().is2xxSuccessful()) {
                orderService.cancel(orderId);
                msg = "OrderCancelComplete";
            }else{
                log.error("HttpStatus = " + response.getStatusCodeValue() + " 결제 취소 실패");
                msg = "PaymentCancelFailed";
            }

        }catch(Exception e){
            log.error("결제 취소가 실패했습니다. 익셉션 발생 -> " + e.getMessage());
            msg = "PaymentCancelFailed";
        }

        return msg;
    }

}
