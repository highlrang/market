package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.cart.CartResponseDto;
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

    private final UserRepository userRepository;
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

    public CartResponseDto findByUser(Long userId) {
        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("CartByUserNotFoundException"));
        return new CartResponseDto(cart);
    }

    public List<Integer> findItemCount(Long userId, List<Long> itemIds){
        return cartItemRepository.findCountByUser_IdAndItem_Id(userId, itemIds);
    }

    @Transactional
    public void put(Long userId, Long itemId, int count, Long couponId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        Coupon coupon = couponRespository.findById(couponId).orElseThrow(() -> new IllegalArgumentException("CouponNowFoundException"));
        CartItem cartItem = CartItem.createCartItem(item, count, coupon);
        try {
            Cart userCart = cartRepository.findById(user.getCart().getId())
                    .orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));

            boolean anyMatch = userCart.getCartItems().stream()
                    .anyMatch(existing -> existing.getItem().equals(item));
            if(!anyMatch) {
                userCart.addCartItem(cartItem); // 연관관계 먼저 !
                cartItemRepository.save(cartItem);
            }

        }catch (IllegalArgumentException e){
            Cart cart = Cart.createCart(user, cartItem);
            cartRepository.save(cart);
        }

    }

    @Transactional
    public Long order(Long userId, List<Long> itemIds){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Delivery delivery = new Delivery(user.getAddress(), DeliveryStatus.READY);

        List<Item> items = itemRepository.findAllById(itemIds);
        List<CartItem> cartItems = user.getCart().getCartItems().stream()
                .filter(cartItem -> items.contains(cartItem.getItem()))
                .collect(Collectors.toList());

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    int price = cartItem.getItem().getPrice();
                    if (cartItem.getCoupon() != null) price -= price * (cartItem.getCoupon().getDiscountPer() / 100);
                    return OrderItem.createOrderItem(cartItem.getItem(), price, cartItem.getCount());
                })
                .collect(Collectors.toList());
        // cartItem 삭제 시 coupon 같이 없어지니까 따로 updateUsed 안 해줌

        Order order = Order.createOrder(user, delivery, orderItems.toArray(OrderItem[]::new));

        return orderRepository.save(order).getId();
    }

    @Transactional
    public void update(Long cartItemId, int count, String couponId){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new IllegalArgumentException("CartItemNotFoundException"));
        if(couponId != null) {
            Coupon coupon = couponRespository.findById(Long.valueOf(couponId)).orElseThrow(() -> new IllegalArgumentException("CouponNotExistException"));
            cartItem.update(count, coupon);
        }else{
            cartItem.update(count, null);
        }
    }

    @Transactional
    public void remove(Long cartId, List<Long> itemIds){
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));

        List<CartItem> cartItems = cartItemRepository.findAllByCart_Id(cartId) // 쿼리에서 일부분 꺼내와서 stream로직 처리!!!
                .stream()
                .filter(cartItem -> itemIds.contains(cartItem.getItem().getId()))
                .collect(Collectors.toList());

        if(!cartItems.isEmpty()) { // 연관관계
            cart.removeCartItems(cartItems);
            cartItemRepository.deleteAll(cartItems);
        }
    }

}
