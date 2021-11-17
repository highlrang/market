package com.myproject.myweb.controller.order;

import com.myproject.myweb.controller.OrderController;
import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.OrderRepository;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// @EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= {OrderController.class,
                          OrderService.class,
                          OrderRepository.class,
                          CustomerRepository.class})
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService orderService;
    @Autowired private OrderController orderController;
    @Autowired private MockMvc mockMvc;
    // @Autowired private MockHttpSession session;

    @Before
    public void setUp(){
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    public void detailview() throws Exception{
        List<Order> orderList = orderRepository.findAll();

        try {
            if(orderList.size() == 0) throw new IllegalStateException();
            Order order = orderList.get(0);

            mockMvc.perform(
                    get("/order/detail/")
                            .param("id", String.valueOf(order.getId())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("detail"))
                    .andExpect(model().attribute("order", new OrderResponseDto(order)));

        }catch (Exception ignored){}
    }

    @Test
    public void listview() throws Exception{
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"));
    }

    @Test
    public void listapi(){
        int page = 1;
        int size = 5;
        try {
            Customer entity = customerRepository.findAll().stream()
                    .filter(c -> c.getOrderList().size() > 9)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            CustomerResponseDto customer = new CustomerResponseDto(entity); // for session

            Page<Order> orderList = orderRepository.findAllByCustomer_Id(customer.getId(), PageRequest.of(page, size));
            ItemService.ListByPaging<OrderResponseDto> body =
                    new ItemService.ListByPaging<>(
                            orderList.getTotalPages(),
                            orderList.getContent().stream()
                                .map(OrderResponseDto::new)
                                .collect(Collectors.toList())
                    );

            MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
            requestParam.add("page", String.valueOf(page));
            requestParam.add("size", String.valueOf(size));

            mockMvc.perform(
                    get("/list/api")
                            .sessionAttr("customer", customer)
                            .params(requestParam))
                    .andExpect(status().isOk())
                    .andExpect(content().json(String.valueOf(body)));

        }catch (Exception ignored){}
    }
}
