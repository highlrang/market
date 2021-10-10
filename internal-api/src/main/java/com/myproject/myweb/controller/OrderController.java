package com.myproject.myweb.controller;

import com.myproject.myweb.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    @GetMapping("/")
    public String home(){
        return "Hello, This is Internal-Api";
    }
}
