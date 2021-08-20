package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orderList;

    @Builder
    public User(String name, String email, String password, Address address){ // 비번 암호화한 후 객체 생성
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
    }

    public void setCart(Cart cart){
        this.cart = cart;
    }

}
