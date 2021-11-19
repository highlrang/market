package com.myproject.myweb.controller.order;

import com.myproject.myweb.controller.OrderController;
import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import com.myproject.myweb.service.user.CustomerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.mail.FetchProfile;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTestByMocking {
    @MockBean private ItemService itemService;
    @MockBean private PaymentService paymentService;
    @MockBean private CartService cartService;
    @MockBean private OrderService orderService;
    @MockBean private Logger log;
    @Autowired private MockMvc mockMvc;
    // @Autowired private MockHttpSession session;

    // 통합 테스트도 하기
    @Test
    public void payment_ready_by_item_page(){
        try {
            int count = 2;
            long mockId = 1L;
            String paymentUrl = "/paymentUrl";

            given(orderService.orderImpossible(mockId))
                    .willReturn(false);

            String couponId = "1";
            given(orderService.order(mockId, mockId, count, couponId))
                    .willReturn(mockId);

            given(paymentService.ready(mockId, mockId))
                    .willReturn(paymentUrl);

            MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
            requestParam.add("customer_id", String.valueOf(mockId));
            requestParam.add("item_id", String.valueOf(mockId));
            requestParam.add("count", String.valueOf(count));
            requestParam.add("coupon", couponId);

            mockMvc.perform(post("/order/payment/ready")
                    .params(requestParam))
                    .andExpect(redirectedUrl(paymentUrl))
                    .andExpect(status().isFound())
                    .andDo(print());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void payment_ready_by_cart_page() throws Exception {
        long mockId = 1L;
        String paymentUrl = "/paymentUrl";
        given(orderService.orderImpossible(mockId))
                .willReturn(false);

        given(cartService.order(mockId, Arrays.asList(mockId)))
                .willReturn(mockId);

        given(paymentService.ready(mockId, mockId))
                .willReturn(paymentUrl);

        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("customer_id", String.valueOf(mockId));
        requestParam.add("item_id", String.valueOf(mockId));
        requestParam.add("cart_id", String.valueOf(mockId));

        mockMvc.perform(post("/order/payment/ready")
                .params(requestParam))
        .andExpect(redirectedUrl(paymentUrl))
        .andExpect(status().isFound())
        .andDo(print());
    }

    @Test
    public void payment_ready_orderImpossible_by_item() throws Exception{
        long mockId = 1L;
        int count = 2;

        given(orderService.orderImpossible(mockId))
                .willReturn(true);

        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("customer_id", String.valueOf(mockId));
        requestParam.add("item_id", String.valueOf(mockId));
        requestParam.add("count", String.valueOf(count));
        // requestParam.add("coupon", couponId);

        String redirectUrl = "/item/detail/" + mockId;

        mockMvc.perform(post("/order/payment/ready")
                .params(requestParam))
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());

    }

    @Test
    public void payment_ready_orderImpossible_by_cart() throws Exception{
        long mockId = 1L;

        given(orderService.orderImpossible(mockId))
                .willReturn(true);

        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("customer_id", String.valueOf(mockId));
        requestParam.add("item_id", String.valueOf(mockId));
        requestParam.add("cart_id", String.valueOf(mockId));

        String redirectUrl = "/cart/detail/" + mockId;

        mockMvc.perform(post("/order/payment/ready")
                .params(requestParam))
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test // item으로만 테스트
    public void payment_ready_throws_ItemStockException() throws Exception{
        long mockId = 1L;
        int count = 2;
        given(orderService.orderImpossible(mockId))
                .willReturn(false);

        given(orderService.order(mockId, mockId, count, "null"))
                .willThrow(ItemStockException.class);

        doNothing().when(itemService).stockNotice(mockId);

        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("customer_id", String.valueOf(mockId));
        requestParam.add("item_id", String.valueOf(mockId));
        requestParam.add("count", String.valueOf(count));

        String redirectUrl = "/item/detail/" + mockId;

        mockMvc.perform(post("/order/payment/ready")
                .params(requestParam))
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test // item으로만 테스트
    public void payment_ready_throws_WebClientException() throws Exception{
        long mockId = 1L;
        int count = 2;
        given(orderService.orderImpossible(mockId))
                .willReturn(false);

        given(orderService.order(mockId, mockId, count, "null"))
                .willReturn(mockId);

        given(paymentService.ready(mockId, mockId))
                .willThrow(WebClientResponseException.class);

        doNothing().when(orderService).remove(mockId);

        MultiValueMap<String, String> requestParam = new LinkedMultiValueMap<>();
        requestParam.add("customer_id", String.valueOf(mockId));
        requestParam.add("item_id", String.valueOf(mockId));
        requestParam.add("count", String.valueOf(count));

        String redirectUrl = "/item/detail/" + mockId;

        mockMvc.perform(post("/order/payment/ready")
                .params(requestParam))
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }
    
    @Test // payment/fail도 같은 로직
    public void payment_cancel() throws Exception{
        long mockId = 1L;
        String redirectUrl = "/item/detail/" + mockId;

        given(orderService.getRedirectUrlByItemOneOrMany(mockId))
                .willReturn(redirectUrl);

        doNothing().when(orderService).remove(mockId);

        mockMvc.perform(get("/order/payment/cancel")
                .param("orderId", String.valueOf(mockId)))
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    public void payment_approve() throws Exception{

    }

    @Test
    public void order_cancel_success() throws Exception{
        long mockId = 1L;

        doNothing().when(paymentService).cancel(mockId);

        mockMvc.perform(get("/order/cancel/" + mockId))
                .andExpect(redirectedUrl("/order/list"))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    public void order_cancel_throws_IllegalStateException() throws Exception{
        long mockId = 1L;

        doThrow(new IllegalStateException("DeliveryAlreadyCompletedException"))
                .when(paymentService).cancel(mockId);

        mockMvc.perform(get("/order/cancel/" + mockId))
                .andExpect(redirectedUrl("/order/list"))
                .andExpect(status().isFound())
                .andDo(print());
    }


}
