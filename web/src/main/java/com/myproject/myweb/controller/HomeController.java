package com.myproject.myweb.controller;

import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
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
public class HomeController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/")
    public String home(@RequestParam(value = "msg", required = false) String msg,
                       Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "home";
    }

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

        Long userId = null;
        try {
            userId = userService.join(userRequestDto);
        }catch (IllegalStateException e){
            bindingResult.rejectValue("email", e.getMessage());
        }

        String msg;
        try {
            userService.certify(userId);
            msg = "UserJoinEmailCertification";

        }catch(MessagingException e){
            log.error(e.getMessage());
            userService.expirateToken(userId);
            msg = "UserJoinCertificationFailed";
        }
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @GetMapping("/certified")
    public String certified(@RequestParam(name = "user") Long userId,
                            @RequestParam(name = "token") String token,
                            RedirectAttributes attributes){
        String msg = "UserJoinComplete";
        if(!userService.confirmToken(userId, token)) {
            msg = "UserJoinCertificationFailed";
        }
        userService.expirateToken(userId);
        attributes.addAttribute("msg", msg);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute UserRequestDto userRequestDto){
        return "user/login";
    }

    @PostMapping("/login")
    public String login(@Valid UserRequestDto userRequestDto,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes attributes){
        if(bindingResult.hasErrors()) return "redirect:/login";

        try{
            UserResponseDto user = userService.login(userRequestDto);
            if(!user.getCertified()) {
                userService.certify(user.getId());
                attributes.addAttribute("msg", "UserJoinEmailCertificationRetry");
                return "redirect:/";
                // 인증 안된채로는 기능 사용 못하게 xx
            }
            session.setAttribute("user", user);

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


}
