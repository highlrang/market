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

    private int count;

    @OneToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    public void setCart(Cart cart) { // 연관관계 매핑용
        this.cart = cart;
    }

    public static CartItem createCartItem(Item item, int count){
        CartItem cartItem = new CartItem();
        cartItem.item = item;
        cartItem.count = count;
        return cartItem;
    }

    // 쿠폰 없을 수 있으니까 setter로 빼놓아야함. 연관관계 주인이 주도
    public void setCoupon(Coupon coupon){
        if(coupon.getCartItem() != null){ // 다른 장바구니 상품이 쿠폰을 사용하고 있다면 제거
            coupon.getCartItem().removeCoupon();
        }
        this.coupon = coupon;
        coupon.setCartItem(this);
    }

    public void removeCoupon(){
        this.coupon = null;
    }

    public void update(int count, Coupon coupon){
        this.count = count;
        this.coupon = coupon;
    }

    public int getTotalPrice(){
        if(coupon == null) return item.getPrice() * count;
        return (item.getPrice() - (int)(item.getPrice() * coupon.getDiscountPer()/100.0)) * count;
    }

}
