package com.myproject.myweb.service.order;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest // (classes = {@TestConfiguration파일.class}) || @Import(@TestConfiguration 파일)
@Transactional
public class OrderServiceIntegrationTest {
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private CustomerRepository customerRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private OrderService orderService;

    // @BeforeEach
    public void setUp(){
        Customer customer = Customer.builder().build();
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        Item item = Item.createItem(
                Category.ANIMAL_GOODS,
                Seller.builder().build(),
                "test item",
                10000,
                100);
        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
    }

    @Test
    public void orderRemove() {
        this.setUp();
        Long orderId = orderService.order(1L, 1L, 1, "null");

        // when
        orderService.remove(orderId);

        // then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> orderService.findById(orderId));
    }

    @Test
    public void orderWithMockCoupon(){
        this.setUp();
        Coupon createCoupon = Coupon.createCoupon("test coupon", Customer.builder().build(), 10, LocalDateTime.now().plusMonths(1));
        Coupon coupon = couponRepository.save(createCoupon);

        Long orderId = orderService.order(1L, 1L, 1, String.valueOf(coupon.getId()));

        OrderResponseDto result = orderService.findById(orderId);
        CouponDto resultCoupon = result.getOrderItems().get(0).getCoupon();
        assertThat(resultCoupon.getId()).isEqualTo(coupon.getId());
        assertThat(resultCoupon.getIsUsed()).isEqualTo(Boolean.TRUE);
    }
}
