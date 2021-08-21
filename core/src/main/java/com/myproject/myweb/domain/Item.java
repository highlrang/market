package com.myproject.myweb.domain;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private int stock;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Photo> photoList = new ArrayList<>();

    @Builder
    public Item(String name, int price, int stock){
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // item 등록 시 n개의 사진 이름 변경 후 추가
    public void setPhotoList(List<Photo> photos){
        this.photoList = photos;
    }

    public void addStock(int stockQuantity){
        stock += stockQuantity;
    }

    public void removeStrock(int stockQuantity){
        int nowStock = stock - stockQuantity;
        if(nowStock < 0){
            throw new IllegalStateException("stock zero");
        }
        stock = nowStock;
    }
}
