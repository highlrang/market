package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.Address;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@NoArgsConstructor
public abstract class UserResponseDto implements UserDetails { // abstract ?

    protected Long id;
    protected String name;
    protected String email;
    protected String password;
    protected Boolean certified;
    protected Address address;

}
