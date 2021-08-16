package com.myproject.myweb.domain;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    private String originName; // 원본 파일명
    private String name; // 저장 파일명
    private String path; // 저장 경로

    @Builder
    public Photo(String originName, String name, String path){
        this.originName = originName;
        this.name = name;
        this.path = path;
    }
}
