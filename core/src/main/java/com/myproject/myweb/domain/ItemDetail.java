package com.myproject.myweb.domain;

import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Embeddable // @Inheritance 공부하기
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ItemDetail { // 옷 컬러별로 있을 경우 달라지는 정보들

    @NotNull
    private int stock;

    @Enumerated(EnumType.STRING)
    private Size size;

    @Embedded
    private File file;

    @ColumnDefault("false")
    private Boolean soldOut;

    @Builder
    public ItemDetail(int stock, Size size, File file){
        this.stock = stock;
        this.size = size;
        this.file = file;
        soldOut = false; // default
    }

    public void soldout_complete(){ // stock 0 또는 사용자 지정
        soldOut = true;
    }

    public void addStock(int stockQuantity){
        stock += stockQuantity;
    }

    public void removeStrock(int stockQuantity){
        stock -= stockQuantity;
    }
}
