package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class CouponServiceIntegrationTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CouponService couponService;

    @Test
    public void find_available_coupon(){
        Customer customer = customerRepository.save(Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test pw")
                .build()
        );
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build()
        );
        Item item = itemRepository.save(Item.createItem(Category.BATHROOM_SUPPLIES,
                seller, "test item", 1000, 100));

        Coupon coupon = couponRepository.save(
                Coupon.createCoupon("selected coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );

        couponRepository.save(
                Coupon.createCoupon("expired coupon", customer, 10, LocalDateTime.now().minusDays(1))
        );
        Coupon usedCoupon = couponRepository.save(
                Coupon.createCoupon("used coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );
        usedCoupon.setIsUsed(true);


        CartItem cartItem = CartItem.createCartItem(item, 1);
        Cart cart = cartRepository.save(
                Cart.createCart(customer, cartItem)
        );
        cart.setCustomer(customer);
        List<CartItem> cartItemList = cartItemRepository.findAllByCart_Id(cart.getId());
        cartItemList.get(0).setCoupon(coupon);


        long cnt = couponRepository.findAll().stream()
                .filter(c -> c.getCustomer().equals(customer)).count();
        assertThat(cnt).isEqualTo(3);
        List<CouponDto> availableCoupon =
                couponService.findAvailableCouponByCustomer(customer.getId());
        assertThat(availableCoupon.size()).isEqualTo(0);
    }

    @Test
    public void find_available_coupon_with_no_cartItem(){
        Customer customer = customerRepository.save(Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test pw")
                .build()
        );
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build()
        );
        Item item = itemRepository.save(Item.createItem(Category.BATHROOM_SUPPLIES,
                seller, "test item", 1000, 100));

        couponRepository.save(
                Coupon.createCoupon("test coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );
        couponRepository.save(
                Coupon.createCoupon("expired coupon", customer, 10, LocalDateTime.now().minusDays(1))
        );
        Coupon usedCoupon = couponRepository.save(
                Coupon.createCoupon("used coupon", customer, 10, LocalDateTime.now().plusMonths(1))
        );
        usedCoupon.setIsUsed(true);

        CartItem cartItem = CartItem.createCartItem(item, 1);
        Cart cart = cartRepository.save(
                Cart.createCart(customer, cartItem)
        );
        cart.setCustomer(customer);
        cart.removeCartItems(cart.getCartItems());
        assertThat(customer.getCart().getCartItems().size()).isEqualTo(0);

        long cnt = couponRepository.findAll().stream()
                .filter(c -> c.getCustomer().equals(customer)).count();
        assertThat(cnt).isEqualTo(3);
        List<CouponDto> availableCoupon =
                couponService.findAvailableCouponByCustomer(customer.getId());
        assertThat(availableCoupon.size()).isEqualTo(1);
        assertThat(availableCoupon.get(0).getName()).isEqualTo("test coupon");
    }

}
