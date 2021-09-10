package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.user.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

    private final SellerService sellerService;
    private final ItemService itemService;
    private final FileHandler fileHandler;
    private final MessageSource messageSource;

    @GetMapping("/join")
    public String joinForm(@ModelAttribute UserRequestDto userRequestDto,
                           @RequestParam(value = "msg", required = false) String msg,
                           Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "user/join";
    }

    @PostMapping("/certify")
    public String certify(@Valid UserRequestDto userRequestDto, BindingResult bindingResult,
                          RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "user/join";

        Long sellerId = null;
        try {
            sellerId = sellerService.join(userRequestDto);
        }catch (IllegalStateException e){
            bindingResult.rejectValue("email", e.getMessage());
        }

        String msg;
        try {
            sellerService.certify(sellerId);
            msg = "UserJoinEmailCertification";

        }catch(MessagingException e){
            log.error(e.getMessage() + " 가입 인증 메일 전송 실패");
            sellerService.expireToken(sellerId);
            msg = "UserJoinCertificationFailed";
        }
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @GetMapping("/certified")
    public String certified(@RequestParam(name = "user") Long sellerId,
                            @RequestParam(name = "token") String token,
                            RedirectAttributes attributes){
        String msg = "UserJoinComplete";
        if(!sellerService.confirmToken(sellerId, token)) {
            msg = "UserJoinCertificationFailed";
        }
        sellerService.expireToken(sellerId);
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/item/index") // 판매자의 상품 리스트
    public String categoryList(Model model){
        model.addAttribute("categories", Category.values());
        return "seller/item/index";
    }

    @GetMapping("/item/list/{category}") // 첫 리스트 요청
    public String listByCategory(@PathVariable("category") String category,
                                 HttpSession session, Model model){

        SellerResponseDto seller = (SellerResponseDto) session.getAttribute("seller");
        ItemService.ListByPaging<ItemResponseDto> itemList =
                itemService.findBySellerAndCategory(seller.getId(), Category.valueOf(category), Pageable.ofSize(5));

        model.addAttribute("totalPage", itemList.getTotalPage());
        model.addAttribute("items", itemList.getList());

        model.addAttribute("nowPage", 1);
        model.addAttribute("nowSize", 5);
        model.addAttribute("category", category);

        return "seller/item/list";
    }

    @GetMapping("/item/list/api") // 이후 리스트 요청(페이지 넘겨서)
    @ResponseBody
    public ItemService.ListByPaging<ItemResponseDto> listByCategoryApi(
            @RequestParam("seller_id") Long sellerId, // 이렇게 session이 아닌 값으로 하는 게 날지
            @RequestParam("category") String category,
            Pageable pageable
    ){
        return itemService.findBySellerAndCategory(sellerId, Category.valueOf(category), pageable);
    }


    @GetMapping("/item/detail/{id}")
    public String detail(@PathVariable Long id, Model model){
        ItemResponseDto item = itemService.findById(id);
        model.addAttribute("item", item);
        return "seller/item/detail";
    }

    @GetMapping("/item/update/{id}")
    public String updateForm(@PathVariable Long id, Model model,
                             @RequestParam(name = "msg", required = false) String msg){
        ItemResponseDto item = itemService.findById(id);
        model.addAttribute("item", item);
        if(msg != null) model.addAttribute("msg", msg);
        return "seller/item/update";
    }

    @PostMapping("/item/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(value="name") String name,
                         @RequestParam(value="price") int price,
                         @RequestParam(value="stock") int stock,
                         @RequestParam(value="file") List<MultipartFile> files,
                         @RequestParam(value="photo") List<Long> photoIds){

        List<PhotoDto> namedPhotos;
        try {
            namedPhotos = fileHandler.photoProcess(files);

        }catch(IOException e){
            RedirectAttributes attributes = new RedirectAttributesModelMap();
            attributes.addAttribute("msg", messageSource.getMessage(e.getMessage(), null, Locale.getDefault()));
            return "redirect:/seller/item/update/" + id;
        }

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .photos(namedPhotos)
                .build();

        itemService.update(id, photoIds, itemRequestDto);
        return "redirect:/seller/item/detail/" + id;
    }


}
