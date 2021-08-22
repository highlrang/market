package com.myproject.myweb.dto.order;

import com.myproject.myweb.domain.OrderItem;
import lombok.Getter;

@Getter
public class OrderItemDto {

    private Long id;
    private String name;
    private int count;

    public OrderItemDto(OrderItem entity){
        this.id = entity.getId();
        this.name = entity.getItem().getName();
        this.count = entity.getCount();
    }
}
