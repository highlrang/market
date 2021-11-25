package com.myproject.myweb.service;

import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.repository.SellerRepository;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class PaymentServiceIntegrationTest {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @SpyBean
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;

    private Customer customer;
    private Order order;

    @BeforeEach
    public void setUp(){
        customer = customerRepository.save(
                Customer.builder()
                        .name("test customer")
                        .email("test email")
                        .password("test pw")
                        .build()
        );
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw")
                        .build()
        );
        Item item = itemRepository.save(
                Item.createItem(Category.ELECTRONICS, seller, "test item", 10000, 100)
        );
        order = orderRepository.save(Order.createOrder(
                customer, Delivery.builder().status(DeliveryStatus.READY).build(),
                OrderItem.createOrderItem(
                        item, item.getPrice(), 1
                )
        ));
    }
    
    @Test
    public void ready(){
        try{
            String url = paymentService.ready(customer.getId(), order.getId());
            assertThat(url).contains("https://mockup-pg-web.kakao.com/v1/");
            Order result = orderRepository.findById(order.getId()).get();
            assertThat(result.getTid()).isNotNull();

        }catch(WebClientResponseException e){
            System.out.println(e.getResponseBodyAsString());
        }
    }

    @Test
    public void cancel(){
        int stock = order.getOrderItems().get(0).getItem().getStock();
        paymentService.cancel(order.getId());
        Order result = orderRepository.findById(order.getId()).get();
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(result.getOrderItems().get(0).getItem().getStock()).isEqualTo(stock+1);
    }

    @Test
    public void cancel_at_deliveryComplete(){
        order.setDelivery(Delivery.builder().status(DeliveryStatus.COMP).build());

        assertThatIllegalStateException()
                .isThrownBy(() -> paymentService.cancel(order.getId()));
    }
}