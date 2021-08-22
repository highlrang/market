package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional // 한 개 주문 시
    public Long order(Long userId, Long itemId, int count){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 배송 생성
        Delivery delivery = new Delivery(user.getAddress(), DeliveryStatus.READY);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 배송, 주문상품 넣어서 >> 주문 생성
        Order order = Order.createOrder(user, delivery, orderItem);
        // orderItem 따로 save 안 해도 저장 되는 것??

        return orderRepository.save(order).getId();
    }


    @Transactional
    public void cancelOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.cancel();
    }

}

