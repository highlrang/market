package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.Seller;
import lombok.Getter;

@Getter
public class SellerResponseDto extends UserResponseDto{

    // item list는 따로 조회

    public SellerResponseDto(Seller entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.certified = entity.getCertified();
    }

}
