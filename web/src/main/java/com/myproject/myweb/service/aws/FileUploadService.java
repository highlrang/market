package com.myproject.myweb.service.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.myproject.myweb.dto.item.PhotoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileUploadService {

    private final AwsS3UploadService awsS3UploadService;

    public PhotoDto uploadImage(MultipartFile file) throws IOException, SdkClientException{
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        String fileName = createFileName(file.getContentType());

        InputStream inputStream = file.getInputStream();
        awsS3UploadService.uploadFile(inputStream, objectMetadata, fileName);

        String fileUrl = findFileUrl(fileName);

        return PhotoDto.builder()
                .originName(file.getOriginalFilename())
                .name(fileName)
                .path(fileUrl)
                .build();
    }

    private String createFileName(String contentType){
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String path = "upload/" + now;
        String extension = extractedContentType(contentType);
        String name = path + "/" + System.nanoTime() + extension; // ../nanoTime.png
        return name;
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

    public String findFileUrl(String fileName){
        return awsS3UploadService.getFileUrl(fileName);
    }

    // 로컬 사진 삭제 필요? >> File이 아닌 InputStream 이용함
    private void deleteLocalFile(){

    }

    public void deleteS3File(String fileName){
        awsS3UploadService.removeFile(fileName);
    }
}
