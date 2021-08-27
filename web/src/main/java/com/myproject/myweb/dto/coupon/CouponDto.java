package com.myproject.myweb.dto.coupon;

import com.myproject.myweb.domain.Coupon;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CouponDto {

    private Long id;
    private String name;
    private int discountPercent;
    // private UserResponseDto user;
    private String expirationDate;

    public CouponDto(Coupon entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.discountPercent = entity.getDiscountPer();
        this.expirationDate = entity.getExpirationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    }
}
