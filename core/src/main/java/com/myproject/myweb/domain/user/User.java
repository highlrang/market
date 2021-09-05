package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Cart;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@MappedSuperclass
@NoArgsConstructor
public abstract class User {

    private String name;
    private String email;
    private String password;

    @Embedded
    private Address address;

    private String certificationToken;
    @ColumnDefault("false")
    private Boolean certified;

    public Boolean checkPassword(String password){
        return this.password.equals(password);
    }
}
