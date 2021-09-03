package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Order;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Customer extends User{

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) // 사용자 삭제될 때 쿠폰 같이 삭제
    public List<Coupon> couponList = new ArrayList<>();

    public void setCart(Cart cart){
        this.cart = cart;
    }
}
