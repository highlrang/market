package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.ItemDetail;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ItemRequestDto {
    private Category category;
    private String name;
    private int price;
    private String description;

    private List<ItemDetail> itemDetails;
    /*
    private int stock;
    private Size size;
    private File file;
     */

    @Builder
    public ItemRequestDto(Category category, String name, int price, String description, List<ItemDetail> itemDetails){
        this.category = category;
        this.name = name;
        this.price = price;
        this.description = description;
        this.itemDetails = itemDetails;
    }

    public Item toEntity(){
        return Item.builder()
                .category(category)
                .name(name)
                .description(description)
                .price(price)
                .itemDetails(itemDetails)
                .build();

    }

    public void addItemDetails(List<ItemDetail> itemDetails){
        this.itemDetails = itemDetails;
    }

}
