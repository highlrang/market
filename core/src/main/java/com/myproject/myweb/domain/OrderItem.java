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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Order order;

    private int orderPrice;
    private int count;

    public void setOrder(Order order){
        this.order = order;
    }

    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.item = item;
        orderItem.orderPrice = orderPrice;
        orderItem.count = count;

        item.removeStrock(count);
        return orderItem;
    }

    public void cancel(){
        item.addStock(count);
    }

    public int getTotalPrice(){
        return orderPrice * count;
    }
}
