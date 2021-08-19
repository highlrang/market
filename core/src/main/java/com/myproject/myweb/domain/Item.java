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
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    @NotNull
    private int price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int stock;

    @Enumerated(EnumType.STRING)
    private Size size;

    // @Embedded
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Photo> photoList;

    @ColumnDefault("false")
    private Boolean soldOut;

    @Builder
    public Item(Category category, String name, int price, String description,
                int stock, Size size, List<Photo> photoList){
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.size = size;
        this.photoList = photoList;
        soldOut = false; // default;
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
