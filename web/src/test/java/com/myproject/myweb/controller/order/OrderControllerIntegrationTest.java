package com.myproject.myweb.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.myweb.controller.OrderController;
import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIntegrationTest {
    @Autowired private SellerRepository sellerRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService orderService;
    @Autowired private OrderController orderController;
    @Autowired private MockMvc mockMvc;

    @Test
    public void detail_view() throws Exception{
        Customer customer = customerRepository.save(Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test pw")
                .build());
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build()
        );
        Item item = itemRepository.save(
                Item.createItem(Category.CLOTHES, seller, "test clothes", 10000, 500)
        );
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), 1);
        Order order = orderRepository.save(Order.createOrder(customer, Delivery.builder().status(DeliveryStatus.READY).build(), orderItem));

        try {
            OrderResponseDto orderDto = new OrderResponseDto(order);

            ResultActions resultActions = mockMvc.perform(get("/order/detail/" + orderDto.getId()));
            resultActions.andExpect(status().isOk())
                    .andExpect(view().name("order/detail"))
                    .andExpect(model().attributeExists("order"))
                    .andDo(print());
            MvcResult mvcResult = resultActions.andReturn();
            Map<String, Object> modelMap = mvcResult.getModelAndView().getModel();
            OrderResponseDto model = (OrderResponseDto) modelMap.get("order");
            assertThat(model.getOrderItems().get(0).getName()).isEqualTo(orderDto.getOrderItems().get(0).getName());
            assertThat(model.getOrderDate()).isEqualTo(orderDto.getOrderDate());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void list_view() throws Exception{
        mockMvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/list"))
                .andDo(print());
    }

    @Test
    public void list_api(){
        int page = 1;
        int size = 5;
        Customer customer = customerRepository.save(Customer.builder()
                    .name("test customer")
                    .email("test email")
                    .password("test pw")
                    .build());
        CustomerResponseDto customerDto = new CustomerResponseDto(customer);

        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build()
        );
        Item item = itemRepository.save(Item.createItem(Category.ANIMAL_GOODS, seller, "test item", 10000, 100));

        List<Order> orders = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), 1);
        Order order = Order.createOrder(customer, Delivery.builder().status(DeliveryStatus.READY).build(), orderItem);
        for(int i=0; i<10; i++) {
            orders.add(order);
        }
        orderRepository.saveAll(orders);

        Page<Order> orderList = orderRepository.findAllByCustomer_Id(customer.getId(), PageRequest.of(0, size));
        ItemService.ListByPaging<OrderResponseDto> body =
                    new ItemService.ListByPaging<>(
                            orderList.getTotalPages(),
                            orderList.getContent().stream()
                                .map(OrderResponseDto::new)
                                .collect(Collectors.toList())
                    );


        try{
            MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
            requestParam.add("page", String.valueOf(page));
            requestParam.add("size", String.valueOf(size));
            ObjectMapper objectMapper = new ObjectMapper();
            String result = objectMapper.writeValueAsString(body);
            mockMvc.perform(
                    get("/order/list/api")
                            .sessionAttr("customer", customerDto)
                            .params(requestParam))
                    .andExpect(status().isOk())
                    .andExpect(content().json(result))
                    .andDo(print());

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
