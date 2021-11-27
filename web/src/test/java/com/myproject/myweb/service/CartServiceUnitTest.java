package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CartServiceUnitTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CouponRepository couponRepository;
    @InjectMocks private CartService cartService;

    @Test
    public void put_with_cart() {
        long mockId = 1L;
        Customer customer = Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test password")
                .build();
        Seller seller = Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build();
        Item item = Item.createItem(
                Category.BATHROOM_SUPPLIES, seller, "test item", 10000, 100);
        Cart cart = Cart.createCart(
                customer, CartItem.createCartItem(item, 1));
        cart.setCustomer(customer);
        assertThat(customer.getCart()).isEqualTo(cart);

        Item saveItem = Item.createItem(
                Category.CLOTHES, seller, "test save item", 10000, 100);
        given(customerRepository.findById(mockId))
                .willReturn(Optional.of(customer));
        given(itemRepository.findById(mockId))
                .willReturn(Optional.of(saveItem));
        given(cartRepository.findById(any()))
                .willReturn(Optional.of(cart));
        given(cartItemRepository.save(any(CartItem.class)))
                .willReturn(null);

        cartService.put(mockId, mockId, 1, "null");
        assertThat(cart.getCartItems().size()).isEqualTo(2);
    }

    @Test
    public void put_with_coupon(){
        long mockId = 1L;
        Customer customer = Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test password")
                .build();
        Seller seller = Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build();
        Item item = Item.createItem(
                Category.BATHROOM_SUPPLIES, seller, "test item", 10000, 100);
        Coupon coupon = Coupon.createCoupon(
                "test coupon", customer, 10, LocalDateTime.now().plusMonths(1)
        );

        given(customerRepository.findById(mockId))
                .willReturn(Optional.of(customer));
        given(itemRepository.findById(mockId))
                .willReturn(Optional.of(item));
        given(couponRepository.findById(mockId))
                .willReturn(Optional.of(coupon));
        given(cartRepository.save(any(Cart.class)))
                .willReturn(null);

        cartService.put(mockId, mockId, 1, String.valueOf(mockId));
        verify(cartRepository).save(any(Cart.class));
    }
}