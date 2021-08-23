package com.myproject.myweb.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UserRequestDto {

    private String email;
    private String password;

    public UserRequestDto(String email, String password){
        this.email = email;
        this.password = password;
    }
}
