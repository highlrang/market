package com.myproject.myweb.controller;

import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute UserRequestDto userRequestDto){
        return "login";
    }

    @PostMapping("/login")
    public String login(UserRequestDto userRequestDto, HttpSession session, BindingResult bindingResult){
        try{
            UserResponseDto user = userService.login(userRequestDto);
            session.setAttribute("user", user);

        }catch (IllegalArgumentException | IllegalStateException error){
            if(error.getMessage().equals("UserNotFoundException")){
                bindingResult.rejectValue("email", "", "email을 가진 사용자를 찾을 수 없습니다.");
            }else{
                bindingResult.rejectValue("password", "", "비밀번호 불일치입니다.");
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
