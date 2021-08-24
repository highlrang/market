package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public OrderResponseDto findById(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        return new OrderResponseDto(order);
    }

    public Boolean orderImpossible(Long userId){
        return orderRepository.findByUserAndStatusReady(userId, OrderStatus.READY).isPresent();
    }

    @Transactional
    public Long order(Long userId, List<Long> itemIds, List<Integer> counts){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        List<Item> items = itemRepository.findAllById(itemIds);

        // 배송 생성
        Delivery delivery = new Delivery(user.getAddress(), DeliveryStatus.READY);

        // 주문상품 생성
        List<OrderItem> orderItems = new ArrayList<>();
        for(int i=0; i<items.size(); i++) {
            orderItems.add(
                    OrderItem.createOrderItem(items.get(i), items.get(i).getPrice(), counts.get(i))
            );
        }

        // 배송, 주문상품 넣어서 >> 주문 생성
        Order order = Order.createOrder(user, delivery, orderItems.toArray(OrderItem[]::new));
        // orderItem 따로 save 안 해도 저장됨(Cascade)

        return orderRepository.save(order).getId();
    }

    @Transactional
    public void saveTid(Long orderId, String tid){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.setTid(tid);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.setOrderStatus(status);
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
        orderRepository.delete(order);
    }
}

