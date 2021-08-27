package com.myproject.myweb.domain;

import jdk.jfr.Name;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // @NotNull
    private int count;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // coupon 삭제되는지 확인
    private Coupon coupon;

    public void setCart(Cart cart) { // 이후에 Cart 엔티티 생성 또는 호출해서 등록 시 사용됨
        this.cart = cart;
    }

    public static CartItem createCartItem(Item item, int count, Coupon coupon){
        CartItem cartItem = new CartItem();
        cartItem.item = item;
        cartItem.count = count;
        cartItem.coupon = coupon;
        return cartItem;
    }

    public void update(int count, Coupon coupon){
        this.count = count;
        this.coupon = coupon;
    }

    public int getTotalPrice(){
        return (item.getPrice() - (item.getPrice() * coupon.getDiscountPer()/100)) * count;
    }



}
