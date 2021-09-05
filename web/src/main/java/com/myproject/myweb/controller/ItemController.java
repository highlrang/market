package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.coupon.CouponDto;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.CouponService;
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
    private final CouponService couponService;
    private final FileHandler fileHandler;
    private final MessageSource messageSource;

    @GetMapping("/save")
    public String saveForm(Model model){
        model.addAttribute("categories", Category.values());
        return "item/save";
    }


    @PostMapping("/save")
    public String save(@RequestParam(value="category") String category,
                       @RequestParam(value="seller_id") Long sellerId, // form으로 생성하기
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
                .category(Category.valueOf(category))
                .sellerId(sellerId)
                .name(name)
                .price(price)
                .stock(stock)
                .photos(namedPhotos)
                .build();

        itemService.save(itemRequestDto);

        return "redirect:/item/list";

    }

    @GetMapping("/list") // 판매자 상관없이 카테고리별 상품 목록
    public String list(@RequestParam(name = "category") String category,
                        @RequestParam(name = "page") int page,
                        @RequestParam(name = "size") int size,
                        Model model){
        List<ItemResponseDto> items = itemService.findAllByCategory(Category.valueOf(category), page, size);
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
            List<CouponDto> coupons = couponService.findByCustomerAndCanUse(customer.getId());
            model.addAttribute("coupons", coupons);
        }

        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "item/detail";
    }
}
