package com.myproject.myweb.scheduler;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredCouponRemoveScheduler {

    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 1 * * *") // 매월 1일
    public void couponRemove(){
        List<Coupon> coupons = couponRepository.findAllByExpirationDateBefore(LocalDateTime.now());
        couponRepository.deleteAllInBatch(coupons); // in query로 해야하는지 확인
    }
}
