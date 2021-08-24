package com.myproject.myweb.dto.cart;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.dto.user.UserResponseDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CartResponseDto {

    private Long id;

    private UserResponseDto user; // no need

    private List<CartItemDto> cartItems;

    private int totalPrice;

    public CartResponseDto(Cart entity){
        this.id = entity.getId();
        this.cartItems = entity.getCartItems()
                .stream()
                .map(CartItemDto::new)
                .collect(Collectors.toList());
        this.user = new UserResponseDto(entity.getUser());
        this.totalPrice = entity.getTotalPrice();
    }
}
