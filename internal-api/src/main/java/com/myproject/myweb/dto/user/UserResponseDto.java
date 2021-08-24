package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.User;
import lombok.Getter;

@Getter
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;

    private Long cartId;

    public UserResponseDto(User entity){
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.cartId = entity.getCart().getId();
    }
}
