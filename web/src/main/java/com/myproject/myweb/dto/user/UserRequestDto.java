package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.Address;
import com.myproject.myweb.domain.user.User;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter @Setter
@NoArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "필수 입력값입니다.") @Email(message = "이메일 형식이어야 합니다.")
    private String email;
    @NotBlank(message = "필수 입력값입니다.")
    private String password;
    private String name;

    private Address address; // 나중에

    public User toEntity(){
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }
}
