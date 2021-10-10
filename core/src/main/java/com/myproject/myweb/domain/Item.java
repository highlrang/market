package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.exception.ItemStockException;
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

    @Enumerated(value = EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    private String name;

    private int price; // origin이랑 now랑 나눠서

    private int stock;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Photo> photoList = new ArrayList<>();

    public static Item createItem(Category category, Seller seller, String name, int price, int stock){
        Item item = new Item();
        item.category = category;
        item.setSeller(seller);
        item.name = name;
        item.price = price;
        item.stock = stock;
        return item;
    }

    public void setSeller(Seller seller){
        this.seller = seller;
        seller.getItemList().add(this);
    }

    public void addStock(int stockQuantity){
        stock += stockQuantity;
    }

    public void removeStock(int stockQuantity){
        int nowStock = stock - stockQuantity;
        if(nowStock < 0){
            throw new ItemStockException("StockZeroException", String.valueOf(id), name);
        }
        stock = nowStock;
    }

    public void update(String name, int price, int stock, List<Photo> photoList){
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.photoList.addAll(photoList);
    }
}
