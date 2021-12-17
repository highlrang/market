package com.myproject.myweb.service.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.PhotoRepository;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.SellerNoticeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceIntegrationTest {
    @Autowired private SellerRepository sellerRepository;
    @Autowired private SellerNoticeRepository sellerNoticeRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private PhotoRepository photoRepository;
    @Autowired private ItemService itemService;
    @Autowired private SellerNoticeService sellerNoticeService;

    @Test
    public void photo_delete_one(){
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build());
        Item item = itemRepository.save(
                Item.createItem(Category.CLOTHES, seller, "test item", 10000, 100)
        );

        Photo photo = Photo.builder()
                .originName("test originName")
                .name("test photo name")
                .path("test photo path")
                .build();
        photo.setItem(item);
        Photo photo2 = Photo.builder()
                .originName("test remove this photo")
                .name("test remove")
                .path("test remove")
                .build();
        photo2.setItem(item);
        photoRepository.saveAll(Arrays.asList(photo, photo2));

        assertThat(item.getPhotoList().size()).isEqualTo(2);

        // 남은 사진 전달
        itemService.deleteOtherPhoto(item.getId(), Arrays.asList(photo.getId()));
        assertThat(item.getPhotoList().size()).isEqualTo(1);
        assertThat(item.getPhotoList().get(0).getName()).isEqualTo("test photo name");
    }


    @Test
    public void item_list_by_seller(){
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build());
        Seller otherSeller = sellerRepository.save(Seller.builder()
                .name("test other seller")
                .email("test email")
                .password("test pw").build());

        itemRepository.save(
                Item.createItem(Category.ELECTRONICS, otherSeller, "test item", 10000, 100)
        );
        itemRepository.save(
                Item.createItem(Category.ELECTRONICS, otherSeller, "test other item", 10000, 100)
        );
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));

        // then
        ItemService.ListByPaging<ItemResponseDto> result =
                itemService.findByCategoryAndSeller(seller.getId(), Category.ELECTRONICS, pageRequest);
        assertThat(result.getList().size()).isEqualTo(0);
        assertThat(result.getTotalPage()).isEqualTo(0);

        ItemService.ListByPaging<ItemResponseDto> result2 =
                itemService.findByCategoryAndSeller(otherSeller.getId(), Category.ELECTRONICS, pageRequest);
        Boolean anyMatch =
                result2.getList().stream().anyMatch(r -> !r.getSeller().getId().equals(otherSeller.getId()));
        assertThat(anyMatch).isEqualTo(false);

        ItemService.ListByPaging<ItemResponseDto> result3 =
                itemService.findByCategoryAndSeller(otherSeller.getId(), Category.HOUSEHOLD_GOODS, pageRequest);
        assertThat(result3.getList().size()).isEqualTo(0);

    }

    @Test
    public void stock_notice(){
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build()
        );
        Item item = itemRepository.save(
                Item.createItem(Category.ELECTRONICS, seller, "test item", 10000, 100)
        );
        itemService.stockNotice(item.getId());

        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        ItemService.ListByPaging<SellerNoticeDto> notices =
                sellerNoticeService.findAllBySeller(seller.getId(), pageRequest);

        assertThat(notices.getTotalPage()).isEqualTo(1);
        assertThat(notices.getList().size()).isEqualTo(1);
        SellerNoticeDto notice = notices.getList().get(0);
        assertThat(notice.getSeller().getId()).isEqualTo(seller.getId());
        assertThat(notice.getTitle()).isEqualTo("재고 관련 알림");
        assertThat(notice.getConfirm()).isFalse();
    }

}
