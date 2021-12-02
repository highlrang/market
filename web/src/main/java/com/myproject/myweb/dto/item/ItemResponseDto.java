package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.dto.user.SellerResponseDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ItemResponseDto {

    private Long id;
    private SellerResponseDto seller;
    private Category category;
    private String name;
    private int price;
    private int stock;
    private List<PhotoDto> photos;

    public ItemResponseDto(Item item){
        id = item.getId();
        category = item.getCategory();
        seller = new SellerResponseDto(item.getSeller());
        name = item.getName();
        price = item.getPrice();
        stock = item.getStock();
        photos = item.getPhotoList()
                .stream()
                .map(PhotoDto::new)
                .collect(Collectors.toList());

    }
}
