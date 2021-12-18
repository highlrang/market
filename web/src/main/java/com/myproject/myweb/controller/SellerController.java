package com.myproject.myweb.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.myproject.myweb.domain.Category;
import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.dto.item.ItemResponseDto;
import com.myproject.myweb.dto.item.PhotoDto;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.exception.AwsSesMailSendingException;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.SellerNoticeService;
import com.myproject.myweb.service.aws.FileUploadService;
import com.myproject.myweb.service.user.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

    private final SellerService sellerService;
    private final SellerNoticeService sellerNoticeService;
    private final ItemService itemService;
    private final MessageSource messageSource;
    private final FileUploadService fileUploadService;

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "msg", required = false) String msg,
                            Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "seller/login";
    }

    @GetMapping("/join")
    public String joinForm(@ModelAttribute UserRequestDto userRequestDto,
                           @RequestParam(value = "msg", required = false) String msg,
                           Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "seller/join";
    }

    @PostMapping("/certify")
    public String certify(@ModelAttribute @Valid UserRequestDto userRequestDto,
                          BindingResult bindingResult,
                          RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "seller/join";

        Long sellerId;
        try {
            sellerId = sellerService.join(userRequestDto);
        }catch (IllegalStateException e){
            log.error(e.getMessage());
            if(e.getMessage().equals("UserAlreadyExistException")) {
                bindingResult.rejectValue("email", e.getMessage());
            }else{
                attributes.addAttribute("msg", messageSource.getMessage("CommonException", null, Locale.getDefault()));
            }
            return "seller/join";
        }

        String msg;
        try {
            sellerService.certify(sellerId);
            msg = "UserJoinEmailCertification";

        }catch(AwsSesMailSendingException e){
            sellerService.updateCertified(sellerId, false);
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


    /***********************************************************************************/

    @GetMapping("/item/save")
    public String saveForm(Model model){
        model.addAttribute("categories", Category.values());
        return "seller/item/save";
    }


    @PostMapping("/item/save")
    public String save(@RequestParam(value="category") String category,
                       @RequestParam(value="seller_id") Long sellerId, // form으로 생성하기
                       @RequestParam(value="name") String name,
                       @RequestParam(value="price") int price,
                       @RequestParam(value="stock") int stock,
                       @RequestParam(value="file", required = false) List<MultipartFile> files,
                       RedirectAttributes attributes){

        List<PhotoDto> namedPhotos = new ArrayList<>();
        if(files != null) {
            for (MultipartFile file : files) {
                try {
                    PhotoDto photoDto = fileUploadService.uploadImage(file);
                    namedPhotos.add(photoDto);
                } catch (IOException | SdkClientException e) {
                    log.error("File Upload Failed. => " + e.getMessage());
                    attributes.addAttribute("msg", messageSource.getMessage("FileUploadFailedException", null, Locale.getDefault()));
                    return "redirect:/seller/item/save";
                }
            }
        }

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .category(Category.valueOf(category))
                .sellerId(sellerId)
                .name(name)
                .price(price)
                .stock(stock)
                .photos(namedPhotos)
                .build();

        Long id = itemService.save(itemRequestDto);
        return "redirect:/seller/item/detail/" + id;
    }

    @GetMapping("/item/list/{category}")
    public String listByCategory(@PathVariable("category") String category,
                                 Model model){
        model.addAttribute("category", category);
        return "seller/item/list";
    }

    @GetMapping("/item/list/api")
    @ResponseBody
    public ItemService.ListByPaging<ItemResponseDto> listByCategoryApi(
            HttpSession session,
            @RequestParam("category") String category,
            Pageable pageable
    ){
        SellerResponseDto seller = (SellerResponseDto) session.getAttribute("seller");
        PageRequest pageRequest =
                PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        return itemService.findByCategoryAndSeller(seller.getId(), Category.valueOf(category), pageRequest);
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
    public String update(@PathVariable(value="id") Long itemId,
                         @RequestParam(value="name") String name,
                         @RequestParam(value="price") int price,
                         @RequestParam(value="stock") int stock,
                         @RequestParam(value="file", required = false) List<MultipartFile> files,
                         @RequestParam(value="photo", required = false) List<Long> photoIds,
                         RedirectAttributes attributes){

        List<PhotoDto> namedPhotos = new ArrayList<>();

        if(files != null){
            for (MultipartFile file : files) {
                try{
                    namedPhotos.add(fileUploadService.uploadImage(file));
                } catch (IOException | SdkClientException e) {
                    log.error("File Upload Failed. => " + e.getMessage());
                    attributes.addAttribute("msg", messageSource.getMessage("FileUploadFailedException", null, Locale.getDefault()));
                    return "redirect:/seller/item/update/" + itemId;
                }
            }
         }

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .photos(namedPhotos)
                .build();

        itemService.deleteOtherPhoto(itemId, photoIds);
        itemService.update(itemId, itemRequestDto);
        return "redirect:/seller/item/detail/" + itemId;
    }

    @GetMapping("/notice")
    public String notice(){
        return "seller/notice-list";
    }

    @GetMapping("/notice/list/api")
    @ResponseBody
    public ItemService.ListByPaging<SellerNoticeDto> noticeApi(HttpSession session, Pageable pageable){
        SellerResponseDto seller = (SellerResponseDto) session.getAttribute("seller");
        return sellerNoticeService.findAllBySeller(
                seller.getId(),
                PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @PostMapping("/notice/check") // >> ajax 처리하기
    public String noticeCheck(@RequestParam("seller_id") Long sellerId,
                              @RequestParam("id") Long id, HttpSession session){
        sellerNoticeService.readNotice(id);
        session.setAttribute("unreadNotice", sellerNoticeService.countUnreadBySeller(sellerId));
        return "redirect:/seller/notice";
    }

    @PostMapping("/notice/remove")
    public String noticeRemove(@RequestParam("id") Long id){
        sellerNoticeService.remove(id);
        return "redirect:/seller/notice";
    }
}
