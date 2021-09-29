package com.myproject.myweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProfileController {

    private final Environment env;

    @GetMapping("/profile")
    public String profile(){
        List<String> profiles =  Arrays.asList(env.getActiveProfiles()); // 현재 프로파일
        List<String> realProfiles = Arrays.asList("real", "real1", "real2"); // real 프로파일
        String defaultProfile = profiles.isEmpty() ? "default" : profiles.get(0);
        return profiles.stream()
                .filter(realProfiles::contains) // real 프로파일이면 그거 반환
                .findAny()
                .orElse(defaultProfile); // 아니면 default나 현재 프로파일 반환
    }
}
