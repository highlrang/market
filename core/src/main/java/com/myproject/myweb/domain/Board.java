package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GeneratorType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id") // ?
    private User user;

    private Item item;

    @CreatedDate
    private LocalDateTime uploadDate;

    @LastModifiedDate
    private LocalDateTime updateDate;

    @Builder
    public Board(User user, Item item){
        this.user = user;
        this.item = item;
    }



}
