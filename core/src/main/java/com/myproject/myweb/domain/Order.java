package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name="ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime orderDate;

    private String tid;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


    public void setCustomer(Customer customer){
        this.customer = customer;
        customer.getOrderList().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem); // 연관관계 매핑
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    public static Order createOrder(Customer customer, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();
        order.setCustomer(customer);
        order.setDelivery(delivery);
        for(OrderItem orderItem: orderItems){
            order.addOrderItem(orderItem);
        }
        order.orderStatus = OrderStatus.READY;
        order.orderDate = LocalDateTime.now();
        return order;
    }

    public void cancel(){
        if(delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("DeliveryAlreadyCompletedException");
        }
        orderStatus = OrderStatus.CANCEL;
        for(OrderItem orderItem: orderItems){
            orderItem.cancel(); // 도메인 메서드
        }
    }

    public int getTotalPrice(){
        int totalPrice = 0;
        for(OrderItem orderItem: orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    public void setTid(String tid){
        this.tid = tid;
    }

    public void setOrderStatus(OrderStatus status){
        orderStatus = status;
    }
}
