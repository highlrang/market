package com.myproject.myweb.domain;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
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

    @Embedded
    private List<ItemDetail> itemDetails; // List 가능?

    @Builder
    public Item(Category category, String name, int price, String description, List<ItemDetail> itemDetails){
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.itemDetails = itemDetails;
    }
}
