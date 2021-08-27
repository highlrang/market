package com.myproject.myweb.service;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRespository;
    private final UserRepository userRepository;

    public List<CouponDto> findAllByIds(List<Long> couponIds){
        List<Coupon> coupons = couponRespository.findAllById(couponIds);
        return coupons.stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
    }

    public List<CouponDto> findByUserAndCanUse(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        List<Coupon> coupons = user.getCouponList();

        user.getCart().getCartItems()
                .forEach(cartItem -> coupons.remove(cartItem.getCoupon()));

        return coupons.stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
    }

}
