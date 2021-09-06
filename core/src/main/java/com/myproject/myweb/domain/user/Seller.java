package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Item;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Seller extends User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Item> itemList = new ArrayList<>();

    @Builder
    public Seller(String name, String email, String password, Address address) { // 비번 암호화한 후 객체 생성
        this.setName(name);
        this.setEmail(email);
        this.setPassword(password);
        this.setAddress(address);
    }
}
