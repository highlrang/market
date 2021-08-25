package com.myproject.myweb.domain;

import jdk.jfr.Name;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    public void setCart(Cart cart) { // 이후에 Cart 엔티티 생성 또는 호출해서 등록 시 사용됨
        this.cart = cart;
    }

    public static CartItem createCartItem(Item item, int count){
        CartItem cartItem = new CartItem();
        cartItem.item = item;
        cartItem.count = count;
        return cartItem;
    }

    public int getTotalPrice(){
        return count * item.getPrice();
    }



}
