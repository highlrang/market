package com.myproject.myweb.dto.item;

import com.myproject.myweb.domain.Photo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhotoDto {

    private Long id;
    private String originName;
    private String name;
    private String path;

    @Builder
    public PhotoDto(String originName, String name, String path){
        this.originName = originName;
        this.name = name;
        this.path = path;
    }

    public Photo toEntity(){
        return Photo.builder()
                .originName(originName)
                .name(name)
                .path(path)
                .build();
    }

    public PhotoDto(Photo photo){
        this.id = photo.getId();
        this.originName = photo.getOriginName();
        this.name = photo.getName();
        this.path = photo.getPath();
    }
}
