package com.myproject.myweb.dto.cart;

import com.myproject.myweb.domain.CartItem;
import lombok.Getter;

@Getter
public class CartItemDto {

    private Long id;
    private Long itemId;
    private String itemName;
    private int itemCount;
    private int itemPrice;

    private Long userId;
    private Long cartId;

    public CartItemDto(CartItem entity){
        this.id = entity.getId();
        this.itemId = entity.getItem().getId();
        this.itemName = entity.getItem().getName();
        this.itemCount = entity.getCount();
        this.itemPrice = entity.getPrice();

        this.userId = entity.getCart().getUser().getId();
        this.cartId = entity.getCart().getId();

    }
}
