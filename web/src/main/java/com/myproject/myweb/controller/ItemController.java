package com.myproject.myweb.controller;

import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.user.SellerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;
    private final SellerService sellerService;
    private final FileHandler fileHandler;
    private final MessageSource messageSource;

    @GetMapping("/save")
    public String saveForm(){
        return "item/save";
    }


    @PostMapping("/save")
    public String save(@RequestParam(value="seller_id") Long sellerId, // form으로 생성해도 됨
                       @RequestParam(value="name") String name,
                       @RequestParam(value="price") int price,
                       @RequestParam(value="stock") int stock,
                       @RequestParam(value="file") List<MultipartFile> files){

        List<PhotoDto> namedPhotos;
        try {
            namedPhotos = fileHandler.photoProcess(files);

        }catch(IOException e){
            RedirectAttributes attributes = new RedirectAttributesModelMap();
            attributes.addAttribute("msg", messageSource.getMessage(e.getMessage(), null, Locale.getDefault()));
            return "redirect:/save";
        }

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .sellerId(sellerId)
                .name(name)
                .price(price)
                .stock(stock)
                .photos(namedPhotos)
                .build();

        itemService.save(itemRequestDto);

        return "redirect:/item/list";

    }

    @GetMapping("/list") // 판매자 상관없이 전체 상품 목록(나중에 카테고리별로 나누기)
    public String list(Model model){
        List<ItemResponseDto> items = itemService.findAll();
        model.addAttribute("items", items);
        return "item/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(value = "msg", required = false) String msg,
                         HttpSession session,
                         Model model){
        ItemResponseDto item = itemService.findById(id);
        model.addAttribute("item", item);

        CustomerResponseDto customer = (CustomerResponseDto) session.getAttribute("customer");
        if(customer != null){
            model.addAttribute("coupons", customer.getCoupons());
        }

        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "item/detail";
    }
}
