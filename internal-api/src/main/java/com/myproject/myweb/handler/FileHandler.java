package com.myproject.myweb.handler;

import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.dto.item.PhotoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileHandler {

    public List<PhotoDto> photoProcess(List<MultipartFile> photos){

        List<PhotoDto> namedPhotos = new ArrayList<>();

        photos.forEach(
            photo -> {
                if(!photo.isEmpty()) {

                    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String path = "images/" + now; // images/20210817
                    File file = new File(path);
                    if (!file.exists()) file.mkdirs();

                    String extension = extractedContentType(photo.getContentType());

                    String name = path + "/" + System.nanoTime() + extension; // ../nanoTime.png

                    namedPhotos.add(PhotoDto.builder()
                            .originName(photo.getOriginalFilename())
                            .name(name)
                            .path(path)
                            .build()
                    );

                    String absolutePath = new File("").getAbsolutePath() + "\\";
                    file = new File(absolutePath + name);
                    try {
                        photo.transferTo(file);

                    } catch (IOException e) {
                        log.info(e.getMessage());
                    }
                }
            }
        );

        return namedPhotos;

    }

    private String extractedContentType(String contentType) {
        String extension;
        switch (contentType) {
            case "image/jpeg":
                extension = ".jpg";
                break;
            case "image/png":
                extension = ".png";
                break;
            case "image/bmp":
                extension = ".bmp";
                break;
            case "image/gif":
                extension = ".gif";
                break;
            default:
                extension = "";
                break;
        }
        return extension;
    }

}
