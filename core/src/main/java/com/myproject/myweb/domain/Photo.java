package com.myproject.myweb.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originName; // 원본 파일명
    private String name; // 저장 파일명
    private String path; // 저장 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    public Photo(String originName, String name, String path){
        this.originName = originName;
        this.name = name;
        this.path = path;
    }

    public void setItem(Item item){
        this.item = item;
    }
}
