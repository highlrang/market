package com.myproject.myweb.handler;

import com.myproject.myweb.domain.ItemDetail;
import com.myproject.myweb.domain.Photo;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileHandler {

    private Map<Integer, List<Photo>> getPhotoName(Map<Integer, List<File>> photos){
        List<Photo> finalPhotoList = photos.stream()
                .map(photo -> {
                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String path = "images/" + now;
                    File file = new File(path);
                    if(!file.exists()) file.mkdirs();

                    String name = path + "/" + System.nanoTime(); // + 확장자;
                    Photo finalPhoto = Photo.builder()
                            .originName(photo.getOriginName())
                            .name(name)
                            .path(path)
                            .build();


                    String absolutePath = new File("").getAbsolutePath() + "\\";
                    file = new File(absolutePath + name);
                    // multipartFile.transferTo(file);

                    return finalPhoto;
                })
                .collect(Collectors.toList());

        return finalPhotoList;

    }

    // file 있을 때만 호출
    public Map<Integer, List<Photo>> photoProcess(Map<Integer, List<File>> photos){

        Map<Integer, List<Photo>> namedPhotos = getPhotoName(photos);

        return namedPhotos;
    }

}
