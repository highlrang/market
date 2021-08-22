package com.myproject.myweb.dto.order;

import com.myproject.myweb.domain.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {

    private Long id;
    private List<OrderItemDto> orderItems;
    private int totalPrice;
    private int totalCount;

    public OrderResponseDto(Order entity){
        this.id = entity.getId();
        this.orderItems = entity.getOrderItems()
                                .stream()
                                .map(OrderItemDto::new)
                                .collect(Collectors.toList());

        this.totalPrice = entity.getTotalPrice();
        this.totalCount = orderItems.stream()
                                    .mapToInt(OrderItemDto::getCount)
                                    .sum();
    }
}
