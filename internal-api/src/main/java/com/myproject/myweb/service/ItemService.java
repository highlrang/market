package com.myproject.myweb.service;

import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.repository.ItemRepository;
import com.myproject.myweb.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final PhotoRepository photoRepository;

    public ItemResponseDto findById(Long id){

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ItemNotFoundException"));

        return new ItemResponseDto(item);
    }

    public Long save(ItemRequestDto itemRequestDto){

        Item item = itemRequestDto.toEntity();

        List<Photo> photoList = getPhotoList(itemRequestDto, item);
        photoRepository.saveAll(photoList);
        item.setPhotoList(photoList);

        return itemRepository.save(item).getId();
    }

    private List<Photo> getPhotoList(ItemRequestDto itemRequestDto, Item item) {
        return itemRequestDto.getPhotos()
                .stream()
                .map(photoDto -> {
                    Photo photo = photoDto.toEntity();
                    photo.setItem(item);
                    return photo;
                })
                .collect(Collectors.toList());
    }

    public List<ItemResponseDto> findAll(){
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(ItemResponseDto::new)
                .collect(Collectors.toList());
    }
}
