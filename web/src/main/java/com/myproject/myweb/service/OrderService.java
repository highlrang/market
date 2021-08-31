package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.order.PaymentReadyDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CouponRepository couponRepository;

    public OrderResponseDto findById(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        return new OrderResponseDto(order);
    }

    public List<OrderResponseDto> findByUserId(Long userId){
        List<Order> orders = orderRepository.findAllByUser_Id(userId);
        return orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    public Boolean orderImpossible(Long userId){
        return orderRepository.findByUserAndStatusReady(userId, OrderStatus.READY).isPresent();
    }

    @Transactional
    public Long order(Long userId, Long itemId, int count, String couponId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 배송 생성
        Delivery delivery = new Delivery(user.getAddress(), DeliveryStatus.READY);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        if(!couponId.equals("null")){
            Coupon coupon = couponRepository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotFoundException"));
            // coupon.updateUsed(); // orderItem에서 cascade로 쿠폰 삭제돼서 필요없음
            orderItem.setCoupon(coupon);
        }

        // 배송, 주문상품 넣어서 >> 주문 생성
        Order order = Order.createOrder(user, delivery, orderItem); // orderItem 따로 save 안 해도 저장됨(Cascade)

        return orderRepository.save(order).getId();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.setOrderStatus(status);
    }

    public String getRedirectUrlByItemOneOrMany(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        if(order.getOrderItems().size() == 1){
            return "item/detail/" + order.getOrderItems().get(0).getId();
        }
        return "cart/detail" + order.getUser().getCart().getId();
    }

    @Transactional
    public void cancel(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.cancel();
    }

    public OrderResponseDto findOrderReady(Long userId){
        Order order = orderRepository.findByUserAndStatusReady(userId, OrderStatus.READY).orElseThrow(() -> new IllegalArgumentException("OrderByUserNotFoundException"));
        return new OrderResponseDto(order);
    }

    @Transactional
    public void remove(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.cancel(); // 재고 복귀
        orderRepository.delete(order);
    }
}

