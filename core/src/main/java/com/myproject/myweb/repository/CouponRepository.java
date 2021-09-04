package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findAllByExpirationDateBefore(LocalDateTime now); // LessThan
}
