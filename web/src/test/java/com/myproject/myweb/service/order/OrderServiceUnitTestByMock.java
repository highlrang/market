package com.myproject.myweb.service.order;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceUnitTestByMock {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;

    // @Spy - 실체이지만 일부를 mock으로 (ex - find는 DB로 이용할 때)
    // @MockBean @SpyBean + @Autowired // spring container 사용 시, spy는 interface에서 사용 못 함

    @Test
    public void findById() {
        Customer customer = Customer.builder().build();
        Delivery delivery = Delivery.builder().status(DeliveryStatus.READY).build();

        Seller seller = Seller.builder().build();
        Item item = Item.createItem(Category.ANIMAL_GOODS, seller, "item1", 10000, 10);
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), 1);

        Item item2 = Item.createItem(Category.ANIMAL_GOODS, seller, "item2", 5000, 10);
        OrderItem orderItem2 = OrderItem.createOrderItem(item2, item2.getPrice(), 2);

        Order order = Order.createOrder(customer, delivery, orderItem, orderItem2);

        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        try{
            OrderResponseDto dto = orderService.findById(1L);
            assertThat(dto.getTotalCount()).isEqualTo(3);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void findException(){
        given(orderRepository.findById(1L)).willReturn(Optional.empty());
        orderService.findById(1L);
    }

    @Test
    public void createOrder(){
        Customer customer = Customer.builder().build();
        Delivery delivery = Delivery.builder().status(DeliveryStatus.READY).build();

        Seller seller = Seller.builder().build();
        Item item = Item.createItem(Category.ANIMAL_GOODS, seller, "item1", 10000, 10);
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), 1);

        Order order = Order.createOrder(customer, delivery, orderItem);

        given(customerRepository.findById(customer.getId()))
                .willReturn(Optional.of(customer));
        given(itemRepository.findById(item.getId()))
                .willReturn(Optional.of(item));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        Long id = orderService.order(customer.getId(), item.getId(), 1, "null");

        /*
        then(orderRepository)
                .should()
                .save(order); // 다른 객체 생성해서 fail
         */
        then(customerRepository).should().findById(customer.getId());
        then(itemRepository).should().findById(item.getId());
        assertThat(id).isEqualTo(order.getId());
    }

    @Test
    public void orderCancel(){
        int stock = 20;
        Item item = Item.createItem(
                Category.ANIMAL_GOODS,
                Seller.builder().build(),
                "test-item", 10000, stock);
        OrderItem orderItem = OrderItem.createOrderItem(item, 10000, 1);
        Order order = Order.createOrder(
                Customer.builder().build(),
                Delivery.builder().status(DeliveryStatus.READY).build(),
                orderItem);
        assertThat(item.getStock()).isEqualTo(stock-1);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        orderService.cancel(1L);

        then(orderRepository).should().findById(1L);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStock()).isEqualTo(stock);
    }

    @Test
    public void redirectUrlItemVer(){
        Item item = Item.createItem(Category.ANIMAL_GOODS, Seller.builder().build(), "test item", 10000, 10);
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), 1);
        Order order = Order.createOrder(Customer.builder().build(), Delivery.builder().status(DeliveryStatus.READY).build(), orderItem);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        String redirectUrl = orderService.getRedirectUrlByItemOneOrMany(1L);

        assertTrue(redirectUrl.contains("item"));
    }

    // @Test
    public void redirectUrlCartVer(){ // 실제 로직 수정 필요
        Item item = Item.createItem(Category.ANIMAL_GOODS, Seller.builder().build(), "test item", 10000, 10);
        Customer customer = Customer.builder().build();
        Cart.createCart(customer, CartItem.createCartItem(item, 1));

        OrderItem orderItem = OrderItem.createOrderItem(item, 10000, 10);

        Order order = Order.createOrder(customer, Delivery.builder().build(), orderItem);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        String redirectUrl = orderService.getRedirectUrlByItemOneOrMany(1L);

        System.out.println(redirectUrl);
        assertTrue(redirectUrl.contains("cart"));
    }

    @Test
    public void list_0개(){
        Page<Order> orders = Page.empty();
        given(orderRepository.findAllByCustomer_Id(1L, PageRequest.of(1, 5)))
            .willReturn(orders);
        ItemService.ListByPaging<OrderResponseDto> orderListByPaging =
                orderService.findByCustomerAndPaging(1L, PageRequest.of(1, 5));
        assertThat(orderListByPaging.getTotalPage()).isEqualTo(0);
        assertThat(orderListByPaging.getList().size()).isEqualTo(0);
    }

    @Test
    public void list_10개_2페이지(){
        int offset = 1; // controller 기준
        int size = 5;
        List<Order> orders = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(
                Item.createItem(Category.ANIMAL_GOODS, Seller.builder().build(), "test item", 10000, 100),
                10000, 1);
        Order order = Order.createOrder(Customer.builder().build(), Delivery.builder().status(DeliveryStatus.READY).build(), orderItem);
        for(int i=0; i<10; i++) {
            orders.add(order);
        }

        PageRequest pageRequest = PageRequest.of(offset, size);
        Page<Order> ordersByPaging = new PageImpl<>(orders.subList(0, size), pageRequest, orders.size());
        given(orderRepository.findAllByCustomer_Id(1L, pageRequest))
                .willReturn(ordersByPaging);

        ItemService.ListByPaging<OrderResponseDto> orderDtosByPaging =
                orderService.findByCustomerAndPaging(1L, pageRequest);

        assertThat(orderDtosByPaging.getTotalPage()).isEqualTo(orders.size()/size);
        assertThat(orderDtosByPaging.getList().size()).isEqualTo(size);
    }
}