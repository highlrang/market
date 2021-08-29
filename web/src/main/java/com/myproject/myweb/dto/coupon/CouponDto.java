package com.myproject.myweb.dto.coupon;

import com.myproject.myweb.domain.Coupon;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CouponDto {

    private Long id;
    private String name;
    private int discountPer;
    // private UserResponseDto user;
    private String expirationDate;

    public CouponDto(Coupon entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.discountPer = entity.getDiscountPer();
        this.expirationDate = entity.getExpirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    }
}
