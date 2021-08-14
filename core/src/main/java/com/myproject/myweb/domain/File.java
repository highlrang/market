package com.myproject.myweb.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {

    private String originName;
    private String name;
    private String path;

    @Builder
    public File(String originName, String name, String path){
        this.originName = originName;
        this.name = name;
        this.path = path;
    }
}
