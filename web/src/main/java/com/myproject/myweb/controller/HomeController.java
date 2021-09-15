package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MessageSource messageSource;

    @GetMapping("/")
    public String home(@RequestParam(value = "msg", required = false) String msg,
                       Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "home";
    }

    @GetMapping("/api/category/list")
    @ResponseBody
    public Category[] APIcategory(){
        return Category.values();
    }

}
