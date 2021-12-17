package com.myproject.myweb.service.item;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.PhotoRepository;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.ItemService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceUnitTest {
    @Mock private PhotoRepository photoRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private ItemRepository itemRepository;
    @InjectMocks private ItemService itemService;

    @Test
    public void save_with_photo(){
        Seller seller = Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw").build();

        List<PhotoDto> photos = Arrays.asList(
                PhotoDto.builder()
                        .originName("test originName")
                        .name("test name")
                        .path("test path")
                        .build());

        ItemRequestDto itemRequestDto =
                ItemRequestDto.builder()
                        .category(Category.CLOTHES)
                        .sellerId(seller.getId())
                        .name("test item")
                        .stock(100)
                        .price(10000)
                        .photos(photos)
                        .build();

        Item item = Item.createItem(
                Category.CLOTHES, seller, "test item", 100, 10000
        );

        photos.forEach(photoDto -> {
                    Photo photo = photoDto.toEntity();
                    photo.setItem(item);
                });

        given(sellerRepository.findById(null))
                .willReturn(Optional.of(seller));
        given(itemRepository.save(any(Item.class)))
                .willReturn(item);

        itemService.save(itemRequestDto);
        verify(sellerRepository).findById(null);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    public void photo_delete_all(){
        Seller seller = Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build();
        Item item = Item.createItem(Category.CLOTHES, seller, "test item", 10000, 100);
        Photo photo = Photo.builder()
                .originName("test originName")
                .name("test photo name")
                .path("test photo path")
                .build();
        photo.setItem(item);

        given(itemRepository.findById(item.getId()))
                .willReturn(Optional.of(item));
        doNothing().when(photoRepository)
                .deleteAllInBatch();

        itemService.deleteOtherPhoto(item.getId(), null);
        verify(photoRepository).deleteAllInBatch();
        assertThat(item.getPhotoList().size()).isEqualTo(0);
    }

    @Test
    public void update_with_photo(){
        Seller seller = Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build();

        Item item = Item.createItem(Category.HOUSEHOLD_GOODS, seller, "test item", 10000, 100);

        List<PhotoDto> photos = Arrays.asList(
                PhotoDto.builder()
                        .originName("test originName")
                        .name("test name")
                        .path("test path")
                        .build());

        ItemRequestDto itemRequestDto =
                ItemRequestDto.builder()
                        .category(Category.CLOTHES)
                        .sellerId(seller.getId())
                        .name("test update item")
                        .stock(100)
                        .price(10000)
                        .photos(photos)
                        .build();

        given(itemRepository.findById(item.getId()))
                .willReturn(Optional.of(item));

        itemService.update(item.getId(), itemRequestDto);
        assertThat(item.getName()).isEqualTo("test update item");
    }

    @Test
    public void item_list_by_category(){
        Seller seller = Seller.builder().build();
        Item item = Item.createItem(Category.ELECTRONICS, seller, "test item", 10000, 100);
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<Item> items = new PageImpl(
                Arrays.asList(item, item, item, item, item, item),
                pageRequest, 6);

        given(itemRepository.findAllByCategory(eq(Category.ELECTRONICS), any(PageRequest.class)))
            .willReturn(items);

        ItemService.ListByPaging<ItemResponseDto> result =
                itemService.findByCategory(Category.ELECTRONICS, pageRequest);

        assertThat(result.getList().size()).isEqualTo(6);
        assertThat(result.getTotalPage()).isEqualTo(2);

    }
}