package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.ItemDetail;
import lombok.Getter;

import java.util.List;

@Getter
public class ItemResponseDto {

    private Category category;
    private String name;
    private int price;
    private String description;

    private List<ItemDetail> itemDetails;

    public ItemResponseDto(Item item){
        category = item.getCategory();
        name = item.getName();
        price = item.getPrice();
        description = item.getDescription();
        itemDetails = item.getItemDetails();
    }
}
