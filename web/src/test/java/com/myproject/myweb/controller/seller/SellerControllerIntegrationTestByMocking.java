package com.myproject.myweb.controller.seller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import com.myproject.myweb.service.user.SellerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.mail.MessagingException;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SellerControllerIntegrationTestByMocking {
    @MockBean private ItemService itemService;
    @MockBean private SellerService sellerService;
    @Autowired private MockMvc mockMvc;

    @Test
    public void certify_throws_BindingResultErrors() throws Exception{
        UserRequestDto userRequestDto = new UserRequestDto();

        String view = "seller/join";
        mockMvc.perform(post("/seller/certify")
                .param("userRequestDto", String.valueOf(userRequestDto))
        )
                .andExpect(view().name(view))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    public void certify_throws_IllegalStateException() throws Exception{
        given(sellerService.join(any(UserRequestDto.class)))
            .willThrow(new IllegalStateException("이메일중복"));

        String redirectUrl = "/seller/join";
        mockMvc.perform(post("/seller/certify")
                .param("name", "test name")
                .param("email", "email@test.com")
                .param("password", "test pw")
        )
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    public void certify_throws_MessagingException() throws Exception{
        Long mockId = 1l;
        given(sellerService.join(any(UserRequestDto.class)))
                .willReturn(mockId);
        doThrow(MessagingException.class)
                .when(sellerService).certify(mockId);
        doNothing()
                .when(sellerService).expireToken(mockId);

        String redirectUrl = "/?msg=UserJoinCertificationFailed";
        mockMvc.perform(post("/seller/certify")
                .param("name", "test name")
                .param("email", "email@test.com")
                .param("password", "test pw")
        )
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }
    
    @Test
    public void certify_success() throws Exception{
        Long mockId = 1l;
        given(sellerService.join(any(UserRequestDto.class)))
                .willReturn(mockId);
        doNothing()
                .when(sellerService).certify(mockId);
        doNothing()
                .when(sellerService).expireToken(mockId);

        String redirectUrl = "/?msg=UserJoinEmailCertification";
        mockMvc.perform(post("/seller/certify")
                .param("name", "test name")
                .param("email", "email@test.com")
                .param("password", "test pw")
        )
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    public void certified_confirmToken_return_False() throws Exception{
        long mockId = 1;
        String token = "test-token-1234";
        given(sellerService.confirmToken(mockId, token))
                .willReturn(Boolean.FALSE);
        doNothing().when(sellerService).expireToken(mockId);

        String redirectUrl = "/?msg=UserJoinCertificationFailed";
        mockMvc.perform(get("/seller/certified")
                .param("user", String.valueOf(mockId))
                .param("token", token)

        )
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

    @Test
    public void certified_confirmToken_return_True() throws Exception{
        long mockId = 1;
        String token = "test-token-1234";
        given(sellerService.confirmToken(mockId, token))
                .willReturn(Boolean.TRUE);
        doNothing().when(sellerService).expireToken(mockId);

        String redirectUrl = "/?msg=UserJoinComplete";
        mockMvc.perform(get("/seller/certified")
                .param("user", String.valueOf(mockId))
                .param("token", token)

        )
                .andExpect(redirectedUrl(redirectUrl))
                .andExpect(status().isFound())
                .andDo(print());
    }

}
