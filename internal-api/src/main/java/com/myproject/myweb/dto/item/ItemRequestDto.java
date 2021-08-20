package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Item;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ItemRequestDto {
    private String name;
    private int price;
    private int stock;
    private List<PhotoDto> photos;

    @Builder
    public ItemRequestDto(String name, int stock, int price, List<PhotoDto> photos){
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.photos = photos;
    }

    public Item toEntity(){
        return Item.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
    }

}
