package com.myproject.myweb.controller.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.ItemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Transactional
public class SellerControllerIntegrationTest {
    @Autowired private SellerNoticeRepository sellerNoticeRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemService itemService;
    @Autowired private MockMvc mockMvc;

    // photo test 필요

    @Test
    @WithMockUser
    public void item_save() throws Exception{
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw").build()
        );

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();
        params.add("category", String.valueOf(Category.CLOTHES));
        params.add("seller_id", String.valueOf(seller.getId()));
        params.add("name", "test item");
        params.add("price", "10000");
        params.add("stock", "100");

        String redirectUrl = "/seller/item/detail/*";
        mockMvc.perform(post("/seller/item/save")
                .params(params)
                .sessionAttr("seller", new SellerResponseDto(seller))
        )
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern(redirectUrl))
                .andDo(print());

        Optional<Item> item = itemRepository.findAll().stream()
                .filter(i -> i.getSeller().equals(seller))
                .findAny();

        assertTrue(item.isPresent());
        assertThat(item.get().getName()).isEqualTo("test item");
    }

    @Test @WithMockUser
    public void item_update() throws Exception{
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw").build()
        );
        int price = 10000;
        int stock = 100;
        Item item = itemRepository.save(
                Item.createItem(Category.CLOTHES, seller, "test item", price, stock)
        );

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();
        params.add("name", "update item");
        params.add("price", String.valueOf(price - 1000));
        params.add("stock", String.valueOf(stock + 100));

        String redirectUrl = "/seller/item/detail/*";
        mockMvc.perform(post("/seller/item/update/" + item.getId())
                .params(params)
                .sessionAttr("seller", new SellerResponseDto(seller))
        )
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern(redirectUrl))
                .andDo(print());

        Item result = itemRepository.findById(item.getId()).get();
        assertThat(result.getName()).isEqualTo("update item");
        assertThat(result.getPrice()).isEqualTo(price - 1000);
        assertThat(result.getStock()).isEqualTo(stock + 100);
    }

    @Test
    @WithMockUser
    public void notice_list_api() throws Exception{
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw").build()
        );
        SellerResponseDto sellerDto = new SellerResponseDto(seller);

        Category category = Category.CLOTHES;
        Item item = itemRepository.save(
                Item.createItem(category, seller, "test item", 10000, 100)
        );
        ItemResponseDto itemResponseDto = new ItemResponseDto(item);

        ItemService.ListByPaging<ItemResponseDto> result =
                new ItemService.ListByPaging<>(
                        1,
                        Arrays.asList(itemResponseDto)
                );
        ObjectMapper objectMapper = new ObjectMapper();
        String resultToString = objectMapper.writeValueAsString(result);

        mockMvc.perform(get("/seller/item/list/api")
                .sessionAttr("seller", sellerDto)
                .param("category", String.valueOf(category))
                .param("page", "1")
                .param("size", "5")
        )
                .andExpect(status().isOk())
                .andExpect(content().json(resultToString))
                .andDo(print());
    }

    @Test @WithMockUser
    public void notice_check() throws Exception{
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw").build()
        );
        SellerNotice notice = sellerNoticeRepository.save(
                SellerNotice.builder()
                        .seller(seller)
                        .title("test notice")
                        .content("test content").build()
        );

        int cnt = sellerNoticeRepository.countBySellerAndConfirmFalse(seller);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("seller_id", String.valueOf(seller.getId()));
        params.add("id", String.valueOf(notice.getId()));

        mockMvc.perform(post("/seller/notice/check")
            .params(params)
        )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/seller/notice"))
                .andDo(print());

        SellerNotice result =
                sellerNoticeRepository.findById(notice.getId()).get();
        assertThat(result.getTitle()).isEqualTo("test notice");
        assertTrue(result.getConfirm());
        assertThat(sellerNoticeRepository.countBySellerAndConfirmFalse(seller)).isEqualTo(cnt - 1);

    }
}
