package com.myproject.myweb.domain;

import com.myproject.myweb.domain.Photo;
import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.awt.*;
import java.util.List;

@Embeddable // @Inheritance 공부하기
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ItemDetail { // 옷 컬러별로 있을 경우 달라지는 정보들

    @NotNull
    private int stock;

    @Enumerated(EnumType.STRING)
    private Color color;

    @Enumerated(EnumType.STRING)
    private Size size;

    @Embedded
    private List<Photo> photoList;

    @ColumnDefault("false")
    private Boolean soldOut;

    @Builder
    public ItemDetail(int stock, Color color, Size size, List<Photo> photoList){
        this.stock = stock;
        this.color = color;
        this.size = size;
        this.photoList = photoList;
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

    public void addPhotoList(List<Photo> photos){
        this.photoList = photos;
    }
}
