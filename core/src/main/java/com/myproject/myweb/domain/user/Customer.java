package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Customer extends User{

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) // 사용자 삭제될 때 쿠폰 같이 삭제
    public List<Coupon> couponList = new ArrayList<>();

    // 이후에 사용 - 연관관계 매핑용
    public void setCart(Cart cart){
        this.cart = cart;
    }

    @Builder
    public Customer(String name, String email, String password, Address address) { // 비번 암호화한 후 객체 생성
        this.setName(name);
        this.setEmail(email);
        this.setPassword(password);
        this.setAddress(address);
    }
}
