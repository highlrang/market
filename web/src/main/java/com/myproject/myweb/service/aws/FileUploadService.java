package com.myproject.myweb.service.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
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

    public String uploadImage(MultipartFile file){
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        String fileName = createFileName(file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            awsS3UploadService.uploadFile(inputStream, objectMetadata, fileName);

        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("파일 변환 에러 발생으로 업로드 실패"));

        } catch (AmazonServiceException e) {
            log.error("AWS S3 파일 업로드 실패 " + e.getMessage()
                    + " ERROR_CODE : " + e.getErrorCode()
                    + ", ERROR_MESSAGE : " + e.getErrorMessage());

        } catch (SdkClientException e) {
            log.error("AWS Clent로 인한 응답 전달 오류 " + e.getMessage());
        }

        return awsS3UploadService.getFileUrl(fileName);
        // DB에 file_url 필드 생성해서 저장하기
    }

    private String createFileName(String originalFileName){
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String path = "upload/" + now;
        String extension = extractedContentType(originalFileName.substring(originalFileName.lastIndexOf("."))); // photo.getContentType()
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
