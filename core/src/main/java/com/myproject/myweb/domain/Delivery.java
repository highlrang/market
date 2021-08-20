package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.Address;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Builder
    public Delivery(Address address, DeliveryStatus status){
        this.address = address;
        this.status = status;
    }

    public void setOrder(Order order){ // 이후에 연관관계 추가
        this.order = order;
    }
}
