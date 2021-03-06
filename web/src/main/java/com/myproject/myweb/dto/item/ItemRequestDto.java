package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemRequestDto { // template에 전달 안 함
    private Category category;
    private Long sellerId;
    private String name;
    private int price;
    private int stock;
    private List<PhotoDto> photos;

    @Builder
    public ItemRequestDto(Category category, Long sellerId, String name, int stock, int price, List<PhotoDto> photos){
        this.category = category;
        this.sellerId = sellerId;
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.photos = photos;
    }

}
