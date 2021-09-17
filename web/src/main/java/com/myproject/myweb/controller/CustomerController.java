package com.myproject.myweb.controller;

import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.service.user.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final MessageSource messageSource;

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "msg", required = false) String msg,
                            Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "customer/login";
    }

    @GetMapping("/join")
    public String joinForm(@ModelAttribute UserRequestDto userRequestDto,
                           @RequestParam(value = "msg", required = false) String msg,
                           Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "customer/join";
    }

    @PostMapping("/certify")
    public String certify(@Valid UserRequestDto userRequestDto, BindingResult bindingResult,
                          RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "customer/join";

        Long customerId = null;
        try {
            customerId = customerService.join(userRequestDto);
        }catch (IllegalStateException e){
            bindingResult.rejectValue("email", e.getMessage());
        }

        String msg;
        try {
            customerService.certify(customerId);
            msg = "UserJoinEmailCertification";

        }catch(MessagingException e){
            log.error(e.getMessage() + " 가입 인증 메일 전송 실패");
            customerService.expireToken(customerId);
            msg = "UserJoinCertificationFailed";
        }
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @GetMapping("/certified")
    public String certified(@RequestParam(name = "user") Long customerId,
                            @RequestParam(name = "token") String token,
                            RedirectAttributes attributes){
        String msg = "UserJoinComplete";
        if(!customerService.confirmToken(customerId, token)) {
            msg = "UserJoinCertificationFailed";
        }
        customerService.expireToken(customerId);
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    /*
    @PostMapping("/login")
    public String login(@Valid UserRequestDto userRequestDto,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "redirect:/login";

        try{
            CustomerResponseDto customer = customerService.login(userRequestDto);
            if(!customer.getCertified()) {
                customerService.certify(customer.getId());
                attributes.addAttribute("msg", "UserJoinEmailCertificationRetry");
                return "redirect:/";
                // 인증 안된채로는 기능 사용 못하게 xx
            }
            session.setAttribute("customer", customer);

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
     */
}
