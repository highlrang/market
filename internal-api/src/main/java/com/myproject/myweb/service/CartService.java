package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.cart.CartResponseDto;
import com.myproject.myweb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public void put(Long userId, Long itemId, int count){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        CartItem cartItem = CartItem.createCartItem(item, count);
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
    public void remove(Long cartId, List<Long> cartItemIds){
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds); //
        if(!cartItems.isEmpty()) cart.removeCartItems(cartItems);
    }

}
