package com.myproject.myweb.controller;

import com.myproject.myweb.domain.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

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

    @Getter
    static class CategoryResponse{
        private String title;
        private Map<String, String> categories;

        public CategoryResponse(String title, Map<String, String> categories){
            this.title = title;
            this.categories = categories;
        }
    }

    @GetMapping("/api/category/list")
    @ResponseBody
    public CategoryResponse ApiCategory(HttpSession session){
        String user = "";
        if(session.getAttribute("customer") != null) user = "customer";
        if(session.getAttribute("seller") != null) user = "seller";

        Map<String, String> categories = new HashMap<>();
        for (Category category : Category.values()) {
            categories.put(category.getKey(), category.getValue());
        }

        return new CategoryResponse(user, categories);
    }

}
