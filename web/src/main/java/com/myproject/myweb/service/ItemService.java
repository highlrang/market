package com.myproject.myweb.service;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final SellerRepository sellerRepository;

    public ItemResponseDto findById(Long id){

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        return new ItemResponseDto(item);
    }

    @Transactional
    public Long save(ItemRequestDto itemRequestDto){
        Seller seller = sellerRepository.findById(itemRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Item item = Item.createItem(itemRequestDto.getCategory(), seller,
                itemRequestDto.getName(), itemRequestDto.getPrice(), itemRequestDto.getStock());

        List<Photo> photoList = itemRequestDto.getPhotos()
                .stream()
                .map(photoDto -> {
                    Photo photo = photoDto.toEntity();
                    photo.setItem(item);
                    return photo;
                })
                .collect(Collectors.toList());

        // cascade로 photo 까지 save ..
        return itemRepository.save(item).getId();
    }

    public ListByPaging<ItemResponseDto> findAllByCategoryAndPaging(Category category, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Item> items = itemRepository.findAllByCategory(category, pageRequest);

        return new ListByPaging<>(items.getTotalPages(), items.getContent().stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList()));
    }

    @Getter @Setter
    public static class ListByPaging<T>{
        private String category;
        private int nowSize;
        private int nowPage;
        private int totalPage;
        private List<T> list;

        public ListByPaging(int totalPage, List<T> list) {
            this.totalPage = totalPage;
            this.list = list;
        }
    }

    public List<ItemResponseDto> findBySeller(Long sellerId){
        return itemRepository.findAllBySeller_Id(sellerId)
                .stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }

    public void stockNotice(Long itemId){
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));
        Seller seller = item.getSeller();
    }
}
