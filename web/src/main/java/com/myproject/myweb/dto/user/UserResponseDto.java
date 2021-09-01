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

    private Boolean certified;

    public UserResponseDto(User entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        if(entity.getCart() != null) this.cartId = entity.getCart().getId();
        this.coupons = entity.getCouponList() // list니까 null이어도 에러 안남
                .stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
        this.certified = entity.getCertified();
    }
}
