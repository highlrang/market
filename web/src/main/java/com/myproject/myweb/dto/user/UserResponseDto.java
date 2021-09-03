package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto{ // abstract ?

    protected Long id;
    protected String name;
    protected String email;
    protected Boolean certified;
    // protected Address address;

}
