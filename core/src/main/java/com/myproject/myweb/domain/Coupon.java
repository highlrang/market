package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Coupon { // 사용 true 또는 만료기간 지나면 삭제되게 스케줄러

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int discountPer;

    private LocalDateTime expirationDate;

    private Boolean isUsed;

    @Builder
    public Coupon(String name, User user, int discountPer, LocalDateTime expirationDate){
        this.name = name;
        this.user = user;
        this.discountPer = discountPer;
        this.expirationDate = expirationDate;
        isUsed = false;
    }

    public void updateUsed(){
        isUsed = !isUsed;
    }
}
