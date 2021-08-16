package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.ItemDetail;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.domain.Size;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.ItemService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;
    private final FileHandler fileHandler;

    @GetMapping("/save")
    public String saveForm(@ModelAttribute ItemForm itemForm){
        return "itemSaveForm";
    }

    @Getter @NoArgsConstructor
    static class ItemForm {
        int price;
        List<ItemDetailForm> itemDetail;
    }
    @Getter @NoArgsConstructor
    static class ItemDetailForm {
        int index;
        Color color;
        Size size;
        int stock;
        List<File> photos; // File ??
    }

    @PostMapping("/save")
    public String save(ItemForm itemForm){
                       // @RequestParam(value="photo") List<MultipartFile> photos

        Map<Integer, List<File>> photosByIndex = itemForm.getItemDetail().stream()
                .collect(Collectors.toMap(ItemDetailForm::getIndex, ItemDetailForm::getPhotos));

        Map<Integer, List<Photo>> namedPhotos = fileHandler.photoProcess(photosByIndex);

        List<ItemDetail> itemDetails = new ArrayList<>();

        itemForm.getItemDetail().forEach(item -> {
                    List<Photo> photos = namedPhotos.get(item.getIndex());
                    itemDetails.add(
                        ItemDetail.builder()
                                .color(item.getColor())
                                .size(item.getSize())
                                .stock(item.getStock())
                                .photoList(photos)
                                .build()
                    );
                });

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .price(itemForm.getPrice())
                .itemDetails(itemDetails)
                .build();
        itemService.save(itemRequestDto);

        return "redirect:/";

    }
}
