package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class User { // member는 모듈 분리할까?

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    @Embedded
    private Address address;

    // Cart
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id") // 필요??
    private Cart cart;

    // Order
    @OneToMany(mappedBy = "user")
    private List<Order> orderList;
}
