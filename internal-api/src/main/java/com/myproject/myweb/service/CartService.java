package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;


    public void put(Long userId, Long itemId, int count){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        CartItem cartItem = CartItem.createCartItem(item, item.getPrice(), count);

        Cart userCart = user.getCart();
        if (userCart == null) {
            Cart cart = Cart.createCart(user, cartItem);
            cartRepository.save(cart);

        }else{
            userCart.addCartItem(cartItem);
        }

    }

    public void cancel(Long cartId, List<Long> cartItemIds){
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds); //
        cart.removeCartItems(cartItems);
    }


    public Long toOrder(Long userId, List<Long> cartItemIds){
        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Delivery delivery = Delivery.builder().address(user.getAddress()).status(DeliveryStatus.READY).build();

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.createOrderItem(cartItem.getItem(), cartItem.getPrice(), cartItem.getCount()))
                .collect(Collectors.toList());

        Order order = Order.createOrder(user, delivery, orderItems.toArray(OrderItem[]::new));
        orderRepository.save(order);// 여러 상품들 >> 하나의 주문서 생성

        // 장바구니 비우기 위의 cancel로 사용하기??
        Cart cart = cartRepository.findByUser_Id(userId).orElseThrow(() -> new IllegalArgumentException("CartNotFoundException"));
        cart.removeCartItems(cartItems);

        return order.getId();
    }

}
