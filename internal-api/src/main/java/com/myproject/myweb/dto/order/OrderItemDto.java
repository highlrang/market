package com.myproject.myweb.dto.order;

import com.myproject.myweb.domain.OrderItem;
import lombok.Getter;

@Getter
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private String name;
    private int price;
    private int count;

    public OrderItemDto(OrderItem entity){ // OrderItem의 order정보보다는 item위주로
        this.id = entity.getId();
        this.name = entity.getItem().getName();
        this.price = entity.getItem().getPrice();
        this.itemId = entity.getItem().getId();
        this.count = entity.getCount();
    }
}
