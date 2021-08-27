package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.coupon.CouponDto;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;

    private Long cartId;
    private List<CouponDto> coupons;

    public UserResponseDto(User entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.cartId = entity.getCart().getId();
        this.coupons = entity.getCouponList()
                .stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
    }
}
