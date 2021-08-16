package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Cart {

    // 쇼핑몰 DB 모델링
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @OneToMany // 일단 mappedBy 안 해줄려고 함 >> 그랬더니 테이블 생김
    // private List<Item> itemList;
}
