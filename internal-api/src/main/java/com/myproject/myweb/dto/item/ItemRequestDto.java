package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.domain.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ItemRequestDto {
    private Category category;
    private String name;
    private int price;
    private String description;

    private int stock;
    private Size size;
    private Color color;
    private List<Photo> photos;

    @Builder
    public ItemRequestDto(String name, int stock, int price, List<Photo> photos){
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.photos = photos;
    }

    public Item toEntity(){
        return Item.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .photoList(photos)
                .build();

    }

}
