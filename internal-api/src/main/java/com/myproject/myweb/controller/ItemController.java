package com.myproject.myweb.controller;

import com.myproject.myweb.domain.ItemDetail;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;
    private final FileHandler fileHandler;

    @GetMapping("/save")
    public String saveForm(@ModelAttribute ItemRequestDto itemRequestDto){
        return "itemSaveForm";
    }

    @PostMapping("/save")
    public String save(ItemRequestDto itemRequestDto){
                       //, @RequestParam(value="files") List<MultipartFile> filesNotDto){

        List<ItemDetail> finalItemDetails = fileHandler.photoProcess(itemRequestDto.getItemDetails());
        itemRequestDto.addItemDetails(finalItemDetails);

        itemService.save(itemRequestDto);

        return "redirect:/";

    }
}
