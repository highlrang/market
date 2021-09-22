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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public ItemService.ListByPaging<OrderResponseDto> findByCustomerAndPaging(Long customerId, Pageable pageable){
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> orders = orderRepository.findAllByCustomer_Id(customerId, pageRequest);
        return new ItemService.ListByPaging<>(
                orders.getTotalPages(),
                orders.getContent().stream()
                    .map(OrderResponseDto::new)
                    .collect(Collectors.toList())
        );
    }

    public Boolean orderImpossible(Long customerId){
        return orderRepository.findByCustomer_IdAndOrderStatus(customerId, OrderStatus.READY).isPresent();
    }

    @Transactional
    public Long order(Long customerId, Long itemId, int count, String couponId) throws IllegalArgumentException, ItemStockException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        // 배송 생성
        Delivery delivery = new Delivery(customer.getAddress(), DeliveryStatus.READY);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        if(!couponId.equals("null")){
            Coupon coupon = couponRepository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotFoundException"));
            coupon.setIsUsed(true); // 쿠폰 삭제시키지 않기에 사용여부 업데이트
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
        order.cancel(); // 재고 & 쿠폰 복구
    }

    public OrderResponseDto findOrderReady(Long customerId){
        Order order = orderRepository.findByCustomer_IdAndOrderStatus(customerId, OrderStatus.READY).orElseThrow(() -> new IllegalArgumentException("OrderByUserNotFoundException"));
        return new OrderResponseDto(order);
    }

    @Transactional
    public void remove(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
        order.cancel(); // 재고 & 쿠폰 복구
        orderRepository.delete(order);
    }
}

