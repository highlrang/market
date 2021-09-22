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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    // 판매자 상관없이 카테고리별 상품 목록
    @GetMapping("/list/{category}")
    public String list(@PathVariable String category, Model model){
        model.addAttribute("category", category);
        return "item/list";
    }

    @GetMapping("/list/api")
    @ResponseBody
    public ItemService.ListByPaging<ItemResponseDto> listApi(
            @RequestParam(name = "category") String category,
            Pageable pageable // ?page=n&size=n 으로 전달하기
    ){
        return itemService.findByCategoryAndPaging(Category.valueOf(category), pageable);
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
