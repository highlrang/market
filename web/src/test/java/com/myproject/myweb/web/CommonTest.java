package com.myproject.myweb.web;



import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.repository.CouponRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
public class CommonTest{

    @Autowired CouponRepository couponRepository;

    @Test
    public void query_test(){
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);
        Coupon coupon = Coupon.builder()
                .expirationDate(yesterday)
                .build();
        couponRepository.save(coupon);

        List<Coupon> coupons = couponRepository.findAllByExpirationDateBefore(LocalDateTime.now());

        assertThat(coupons.size()).isEqualTo(1);
        assertThat(coupons.get(0).getExpirationDate()).isEqualTo(yesterday);

    }


}