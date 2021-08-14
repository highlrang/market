package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Board;
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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Board> boardList;

    // Cart

    // Order
}
