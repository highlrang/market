package com.myproject.myweb.service;

import com.myproject.myweb.domain.CartItem;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public List<CouponDto> findByCustomerAndCanUse(Long customerId){
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        List<Coupon> coupons = customer.getCouponList();

        /*
        1. cartItem에 사용되지 않은
        2. 기간이 만료되지 않은
        3. 결제에 사용되지 않은(isUsed=false) 쿠폰만 보내기
        */

        try {
            List<Coupon> selectedCoupons = customer.getCart().getCartItems()
                    .stream()
                    .map(CartItem::getCoupon)
                    .collect(Collectors.toList());

            return coupons.stream()
                    .filter(coupon -> coupon.getIsUsed().equals(Boolean.FALSE))
                    .filter(coupon -> coupon.getExpirationDate().isBefore(LocalDateTime.now()))
                    .filter(coupon -> !selectedCoupons.contains(coupon))
                    .map(CouponDto::new)
                    .collect(Collectors.toList());

        }catch (NullPointerException e){
            return coupons.stream()
                    .filter(coupon -> coupon.getIsUsed().equals(Boolean.FALSE))
                    .filter(coupon -> coupon.getExpirationDate().isBefore(LocalDateTime.now()))
                    .map(CouponDto::new)
                    .collect(Collectors.toList());
        }
    }

}
