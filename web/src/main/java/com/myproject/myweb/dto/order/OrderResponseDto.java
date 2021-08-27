package com.myproject.myweb.dto.order;

import com.myproject.myweb.domain.Delivery;
import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {

    private Long id;
    private String deliveryStatus;
    private List<OrderItemDto> orderItems;
    private int totalPrice;
    private int totalCount;
    private String orderDate;
    private String orderStatus;
    private String tid;

    public OrderResponseDto(Order entity){
        this.id = entity.getId();
        this.deliveryStatus = entity.getDelivery().getStatus().getName();
        this.orderItems = entity.getOrderItems()
                                .stream()
                                .map(OrderItemDto::new)
                                .collect(Collectors.toList());

        this.totalPrice = entity.getTotalPrice();
        this.totalCount = orderItems.stream()
                                    .mapToInt(OrderItemDto::getCount)
                                    .sum();
        this.orderDate = entity.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.orderStatus = entity.getOrderStatus().getName();
        this.tid = entity.getTid();
    }

    public String getOrderItemsName(){
        String name = orderItems.get(0).getName();
        if(orderItems.size() > 1) name += " 외 " + orderItems.size() + " 종류의 상품";
        return name;
    }
}
