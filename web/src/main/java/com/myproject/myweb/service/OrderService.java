package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final CouponRepository couponRepository;

    public OrderResponseDto findById(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        return new OrderResponseDto(order);
    }

    public List<OrderResponseDto> findByCustomerId(Long customerId){
        List<Order> orders = orderRepository.findAllByCustomer_Id(customerId);
        return orders.stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }

    public Boolean orderImpossible(Long customerId){
        return orderRepository.findByUserAndStatusReady(customerId, OrderStatus.READY).isPresent();
    }

    @Transactional
    public Long order(Long userId, Long itemId, int count, String couponId) throws IllegalArgumentException, ItemStockException {
        Customer customer = customerRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 배송 생성
        Delivery delivery = new Delivery(customer.getAddress(), DeliveryStatus.READY);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        if(!couponId.equals("null")){
            Coupon coupon = couponRepository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotFoundException"));
            coupon.updateUsed(); // 쿠폰 삭제시키지 않기에 사용여부 업데이트
            orderItem.setCoupon(coupon);
        }

        // 배송, 주문상품 넣어서 >> 주문 생성
        Order order = Order.createOrder(customer, delivery, orderItem); // orderItem 따로 save 안 해도 저장됨(Cascade)

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
        return "cart/detail" + order.getCustomer().getCart().getId();
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

