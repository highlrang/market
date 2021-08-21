package com.myproject.myweb.service;

import com.myproject.myweb.domain.CartItem;
import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
// @ExtendWith(MockitoExtension.class)
class CartServiceTest {

    // @Mock ItemRepository itemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired CartItemRepository cartItemRepository;
    // @Mock OrderRepository orderRepository;
    // @InjectMocks
    @Autowired CartService cartService;

    /*
    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        cartService = new CartService(userRepository, itemRepository, cartRepository, cartItemRepository, orderRepository);
    }
     */

    @Test @Commit
    void put() {
        cartService.put(1L, 3L, 1);
        cartService.put(1L, 4L, 1);

        List<CartItem> cartItems = cartItemRepository.findAll();

        cartItems.forEach(item ->
                System.out.println("사용자 이름 " + item.getCart().getUser().getName() + " 상품 가격 = " + item.getTotalPrice() + " 상품 재고 = " + item.getItem().getStock())
        );
    }

    @Test
    void cancel() {
    }

    @Test
    void toOrder() {

        // Long[] ids = {3L, 4L};
        List<CartItem> cartItems = cartItemRepository.findAllByUser_Id(1L);
        cartService.toOrder(1L, cartItems.stream().map(CartItem::getId).collect(Collectors.toList()));

        // then
        Order order = orderRepository.findAll().get(0);
        System.out.println("사용자 이름 = " + order.getUser().getName());
        order.getOrderItems()
                .forEach(item -> System.out.println("주문한 상품 이름 " + item.getItem().getName() + " 주문한 상품 재고 = " + item.getItem().getStock()));


        // when(postService.findAll()).thenReturn(mockList);
        // doThrow().when().method();
        // verify(postRepository, atLeastOnce()).findAll();
    }
}