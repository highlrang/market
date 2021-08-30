package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
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

    private String certificationToken;
    @ColumnDefault("false")
    private Boolean certified;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<Coupon> couponList = new ArrayList<>();

    @Builder
    public User(String name, String email, String password, Address address){ // 비번 암호화한 후 객체 생성
        this.email = email;
        this.password = password;
        this.name = name;
        this.address = address;
        certified = false;
    }

    public void setCertificationToken(String token){
        certificationToken = token;
    }
    public void setCertified(Boolean isCertified){
        certified = isCertified;
    }

    public void setCart(Cart cart){
        this.cart = cart;
    }

    public Boolean checkPassword(String password){
        return this.password.equals(password);
    }
}
