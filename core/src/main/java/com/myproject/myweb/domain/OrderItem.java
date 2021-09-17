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
    }  // 연관관계 매핑에 사용됨

    public void setCoupon(Coupon coupon){
        this.coupon = coupon;
    }  // 단방향

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.item = item; // 단방향
        orderItem.price = orderPrice;
        orderItem.count = count;

        item.removeStrock(count);
        return orderItem;
    }

    public void cancel(){
        item.addStock(count);
        if(coupon != null) coupon.setIsUsed(false);
    }

    public int getTotalPrice(){
        if(coupon == null) return price * count;
        int finalPrice = price - (price * (coupon.getDiscountPer() / 100)); // 할인가
        return finalPrice * count;
    }
}
