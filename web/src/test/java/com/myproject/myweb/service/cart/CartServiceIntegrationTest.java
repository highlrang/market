package com.myproject.myweb.service.cart;

import com.myproject.myweb.domain.CartItem;
import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.repository.*;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.OrderService;
import groovy.transform.AutoClone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class CartServiceIntegrationTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;

    @Test
    public void coupon_duplicate_calls_in_cart(){
        Customer customer = customerRepository.save(Customer.builder()
                        .name("test customer")
                        .email("test email")
                        .password("test pw").build()
        );
        Coupon coupon = couponRepository.save(
                Coupon.createCoupon("test coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );

        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build()
        );
        Item item = itemRepository.save(
                Item.createItem(Category.HOUSEHOLD_GOODS, seller, "test item", 10000, 100)
        );

        cartService.put(customer.getId(), item.getId(), 1, String.valueOf(coupon.getId()));
        CartItem cartItem = cartItemRepository.findById(coupon.getCartItem().getId()).get();
        assertThat(cartItem.getCoupon()).isEqualTo(coupon);


        Item item2 = itemRepository.save(
                Item.createItem(Category.ANIMAL_GOODS, seller, "test item2", 10000, 100)
        );
        cartService.put(customer.getId(), item2.getId(), 1, String.valueOf(coupon.getId()));

        // then
        assertThat(cartItem.getCoupon()).isNull();
        assertThat(cartItem.getCoupon()).isNotEqualTo(coupon);
        CartItem cartItem2 = cartItemRepository.findById(coupon.getCartItem().getId()).get();
        assertThat(cartItem2.getCoupon()).isEqualTo(coupon);
    }

    @Test
    public void coupon_duplicate_calls_in_order(){
        Customer customer = customerRepository.save(Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test pw").build()
        );
        Coupon coupon = couponRepository.save(
                Coupon.createCoupon("test coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );

        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build()
        );
        Item item = itemRepository.save(
                Item.createItem(Category.HOUSEHOLD_GOODS, seller, "test item", 10000, 100)
        );

        cartService.put(customer.getId(), item.getId(), 1, String.valueOf(coupon.getId()));

        // when
        orderService.order(customer.getId(), item.getId(), 1, String.valueOf(coupon.getId()));

        // then
        Coupon afterCoupon = couponRepository.findById(coupon.getId()).get();
        assertThat(afterCoupon.getCartItem()).isNull();
        boolean anyMatch = cartItemRepository.findAll()
                .stream().anyMatch(c -> c.getCoupon() != null && c.getCoupon().equals(afterCoupon));
        assertThat(anyMatch).isFalse();


    }
}
