package com.myproject.myweb.dto.order;

import com.myproject.myweb.domain.OrderItem;
import com.myproject.myweb.dto.coupon.CouponDto;
import lombok.Getter;

@Getter
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private String name;
    private int price;
    private int count;
    private int totalPrice;
    private CouponDto coupon;

    public OrderItemDto(OrderItem entity){ // OrderItem의 order정보보다는 item위주로
        this.id = entity.getId();
        this.name = entity.getItem().getName();
        this.price = entity.getPrice();
        this.itemId = entity.getItem().getId();
        this.count = entity.getCount();
        this.totalPrice = entity.getTotalPrice();
        if(entity.getCoupon() != null) this.coupon = new CouponDto(entity.getCoupon());
    }
}
