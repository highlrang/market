package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Item;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.user.CustomerService;
import com.myproject.myweb.service.user.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
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
            sellerService.expirateToken(sellerId);
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
        sellerService.expirateToken(sellerId);
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@Valid UserRequestDto userRequestDto,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "redirect:/login";

        try{
            SellerResponseDto seller = sellerService.login(userRequestDto);
            if(!seller.getCertified()) {
                sellerService.certify(seller.getId());
                attributes.addAttribute("msg", "UserJoinEmailCertificationRetry");
                return "redirect:/";
                // 인증 안된채로는 기능 사용 못하게 xx
            }
            session.setAttribute("seller", seller);

        }catch (IllegalArgumentException | IllegalStateException | MessagingException error){

            if(error.getMessage().equals("UserNotFoundException")){
                bindingResult.rejectValue("email", "EmailError", "email을 가진 사용자를 찾을 수 없습니다.");

            }else if(error.getMessage().equals("UserNotMatchedException")){
                bindingResult.rejectValue("password", "PasswordError", "비밀번호 불일치입니다.");

            }else{
                bindingResult.reject("EmailError", "이메일 인증을 위한 이메일 전송이 실패했습니다.");
            }
            return "redirect:/login";
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/item/list") // 판매자의 상품 리스트
    public String list(HttpSession session, Model model){
        SellerResponseDto seller = (SellerResponseDto) session.getAttribute("seller");
        List<ItemResponseDto> itemList = itemService.findBySeller(seller.getId());
        model.addAttribute("items", itemList);
        return "seller/item/list";
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
                         @RequestParam(value="photo") List<PhotoDto> photos){

        log.info(String.valueOf(photos.isEmpty()));

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

        itemService.update(id, itemRequestDto);
        return "redirect:/seller/item/detail/" + id;
    }


}
