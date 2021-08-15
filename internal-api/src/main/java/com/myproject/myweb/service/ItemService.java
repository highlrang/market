package com.myproject.myweb.service;

import com.myproject.myweb.domain.Item;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemResponseDto findById(Long id){
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        return new ItemResponseDto(item);
    }

    public Long save(ItemRequestDto itemRequestDto){
        return itemRepository.save(itemRequestDto.toEntity()).getId();
    }


}
