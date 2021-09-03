package com.myproject.myweb.service;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRespository;
    private final CustomerRepository customerRepository;

    public List<CouponDto> findAllByIds(List<Long> couponIds){
        List<Coupon> coupons = couponRespository.findAllById(couponIds);
        return coupons.stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
    }

    public List<CouponDto> findByUserAndCanUse(Long userId){
        Customer customer = customerRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        List<Coupon> coupons = customer.getCouponList();

        customer.getCart().getCartItems()
                .forEach(cartItem -> coupons.remove(cartItem.getCoupon()));

        return coupons.stream()
                .map(CouponDto::new)
                .collect(Collectors.toList());
    }

}
