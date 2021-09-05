package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CustomerRepository customerRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRespository;
    private final OrderRepository orderRepository;

    public CartResponseDto findById(Long cartId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));
        return new CartResponseDto(cart);
    }

    public CartResponseDto findByCustomer(Long customerId) throws IllegalArgumentException{
        Cart cart = cartRepository.findByCustomer_Id(customerId)
                .orElseThrow(() -> new IllegalArgumentException("CartByUserNotFoundException"));
        return new CartResponseDto(cart);
    }

    public Long findCartIdByCartItemId(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new IllegalArgumentException("CartItemNotFoundException"));
        return cartItem.getCart().getId();
    }

    @Transactional
    public void put(Long customerId, Long itemId, int count, String couponId) throws IllegalArgumentException{
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        CartItem cartItem = CartItem.createCartItem(item, count);
        if(couponId != null) {
            Coupon coupon = couponRespository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotFoundException"));
            cartItem.setCoupon(coupon);
        }

        try {
            Cart userCart = cartRepository.findById(customer.getCart().getId())
                    .orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));

            // boolean anyMatch = userCart.getCartItems().stream().anyMatch(existing -> existing.getItem().equals(item));
            userCart.addCartItem(cartItem); // cascade 설정 안 했기에 연관관계 먼저 넣기
            cartItemRepository.save(cartItem);


        }catch (IllegalArgumentException e){
            Cart cart = Cart.createCart(customer, cartItem);
            cartRepository.save(cart);
        }

    }

    @Transactional
    public Long order(Long customerId, List<Long> itemIds) throws ItemStockException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Delivery delivery = new Delivery(customer.getAddress(), DeliveryStatus.READY);

        List<Item> items = itemRepository.findAllById(itemIds);
        List<CartItem> cartItems = customer.getCart().getCartItems().stream()
                .filter(cartItem -> items.contains(cartItem.getItem()))
                .collect(Collectors.toList());

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = OrderItem.createOrderItem(cartItem.getItem(), cartItem.getItem().getPrice(), cartItem.getCount());
                    if(cartItem.getCoupon() != null) {
                        cartItem.getCoupon().setIsUsed(true); // 쿠폰 삭제시키지 않기에 사용여부 업데이트
                        orderItem.setCoupon(cartItem.getCoupon());
                    }
                    return orderItem;
                })
                .collect(Collectors.toList());

        Order order = Order.createOrder(customer, delivery, orderItems.toArray(OrderItem[]::new));

        return orderRepository.save(order).getId();
    }

    @Transactional
    public void update(Long cartItemId, int count, String couponId) throws IllegalArgumentException{
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new IllegalArgumentException("CartItemNotFoundException"));
        if(!couponId.equals("null")) {
            Coupon coupon = couponRespository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotFoundException"));
            cartItem.update(count, coupon);
        }else{
            cartItem.update(count, null);
        }
    }

    @Transactional
    public void remove(Long cartId, List<Long> itemIds) throws IllegalArgumentException{
        List<CartItem> cartItems = cartItemRepository.findAllByCart_Id(cartId) // 쿼리에서 일부분 꺼내와서 stream로직 처리!!!
                .stream()
                .filter(cartItem -> itemIds.contains(cartItem.getItem().getId()))
                .collect(Collectors.toList());

        if(!cartItems.isEmpty()) {
            // cart.removeCartItems(cartItems); cascade로 삭제 됨
            cartItemRepository.deleteAll(cartItems); // 쿠폰은 삭제 안 되고 남음
        }
    }

}
