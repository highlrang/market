package com.myproject.myweb.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id
    @GeneratedValue
    @Column(name="order_item_id")
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="item_id")
    private Item item;

    @JsonIgnore // 중복이니 ignore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Order order;

    private int price; // 주문 당시 금액
    private int count;

    @OneToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    public void setOrder(Order order){
        this.order = order;
    }

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.item = item;
        orderItem.price = orderPrice;
        orderItem.count = count;

        item.removeStrock(count);
        return orderItem;
    }

    public void setCoupon(Coupon coupon){
        this.coupon = coupon;
    }

    public void cancel(){
        item.addStock(count);
    }

    public int getTotalPrice(){
        if(coupon != null) return price * count;
        int finalPrice = price - (price * (coupon.getDiscountPer() / 100)); // 할인가
        return finalPrice * count;
    }
}
