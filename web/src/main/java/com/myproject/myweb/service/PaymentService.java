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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static com.myproject.myweb.config.Constants.KAKAOPAY_CID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final WebClient webClient = WebClient.create();
    @Value("${webUrl}")
    private String webUrl;

    private MultiValueMap<String, String> getParameterMap(@RequestParam("customer_id") Long customerId, Long orderId) {
        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", KAKAOPAY_CID);
        parameterMap.add("partner_order_id", String.valueOf(orderId));
        parameterMap.add("partner_user_id", String.valueOf(customerId));
        return parameterMap;
    }

    @Transactional
    public String ready(Long customerId, Long orderId) throws WebClientResponseException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        OrderResponseDto orderResponseDto = new OrderResponseDto(order);

        String myHost = webUrl + "/order/payment";
        String kakaopayUrl = "https://kapi.kakao.com/v1/payment/ready";

        MultiValueMap<String, String> parameterMap = getParameterMap(customerId, orderId);
        parameterMap.add("item_name", orderResponseDto.getOrderItemsName());
        parameterMap.add("quantity", String.valueOf(orderResponseDto.getTotalCount()));
        parameterMap.add("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
        parameterMap.add("tax_free_amount", "0");

        parameterMap.add("approval_url", myHost + "/approve");
        parameterMap.add("cancel_url", myHost + "/cancel?orderId=" + orderId);
        parameterMap.add("fail_url", myHost + "/fail?orderId=" + orderId);

        PaymentReadyDto paymentReadyDto = webClient
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
                    .bodyToMono(PaymentReadyDto.class)
                    .block();
                    /*
                    .bodyToMono()
                    .onErrorResume(throwable -> {
                        return Mono.error(new RuntimeException(throwable));
                    });
                    */
                    /*
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                              clientResponse -> clientResponse.toEntity(String.class)
                                                            .map(body -> new RuntimeException(body))
                    )
                     */

        // if(responseEntity.getStatusCode().is2xxSuccessful()) {}
        this.saveTid(orderId, paymentReadyDto.getTid());
        return paymentReadyDto.getNext_redirect_pc_url();
    }

    @Transactional
    public void saveTid(Long orderId, String tid){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.setTid(tid);
    }

    public void approve(Long customerId, Long orderId, String pg_token) throws WebClientResponseException{
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));

        MultiValueMap<String, String> parameterMap = getParameterMap(customerId, orderId);
        parameterMap.add("tid", order.getTid());
        parameterMap.add("pg_token", pg_token);

        String baseUrl = "https://kapi.kakao.com/v1/payment/approve";

        webClient.mutate()
                    .baseUrl(baseUrl)
                    .defaultHeaders(httpHeaders -> {
                        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        httpHeaders.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                    })
                    .build()
                    .post()
                    .body(BodyInserters.fromFormData(parameterMap))
                    .retrieve();
    }

    @Transactional
    public void cancel(Long orderId) throws IllegalStateException, WebClientResponseException{
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));

        MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("cid", KAKAOPAY_CID);
        parameterMap.add("tid", order.getTid());
        parameterMap.add("cancel_amount", String.valueOf(order.getTotalPrice()));
        parameterMap.add("cancel_tax_free_amount", "0");

        webClient.mutate()
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
                .retrieve();

        orderService.cancel(orderId);
    }

}
