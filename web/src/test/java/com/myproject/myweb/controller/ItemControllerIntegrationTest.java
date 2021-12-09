package com.myproject.myweb.controller;


import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.CouponService;
import com.myproject.myweb.service.ItemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Transactional
public class ItemControllerIntegrationTest { // from customer
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemService itemService;
    @Autowired private CouponService couponService;
    @Autowired private MockMvc mockMvc;

    @Test
    public void detail() throws Exception{
        Customer customer = customerRepository.save(Customer.builder()
                .name("test customer")
                .email("test email")
                .password("test pw").build());
        CustomerResponseDto customerDto = new CustomerResponseDto(customer);

        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build());
        Item item = itemRepository.save(
                Item.createItem(Category.CLOTHES, seller, "test item", 10000, 100));
        ItemResponseDto itemDto = new ItemResponseDto(item);

        ResultActions actions = mockMvc.perform(get("/item/detail/" + item.getId())
                .sessionAttr("customer", customerDto)
        );
        actions.andExpect(status().isOk())
                .andExpect(view().name("item/detail"))
                .andExpect(model().attributeExists("item", "coupons"))
                .andDo(print());

        Map<String, Object> modelMap = actions.andReturn().getModelAndView().getModel();
        ItemResponseDto itemResult = (ItemResponseDto) modelMap.get("item");

        assertThat(itemDto.getId()).isEqualTo(itemResult.getId());



    }

}
