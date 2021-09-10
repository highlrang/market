package com.myproject.myweb.dto.user;

import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.coupon.CouponDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomerResponseDto extends UserResponseDto {

    // order list는 따로 조회
    private Long cartId;
    private List<CouponDto> coupons;

    public CustomerResponseDto(Customer entity){
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return getPassword();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
