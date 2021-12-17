package com.myproject.myweb.service.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.myproject.myweb.config.S3Component;
import com.myproject.myweb.service.aws.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class AwsS3UploadService implements UploadService {
    private final AmazonS3Client amazonS3Client;
    private final S3Component component;

    @Override
    public void uploadFile(InputStream inputStream, ObjectMetadata objectMetadata, String fileName) throws AmazonServiceException, SdkClientException{
        amazonS3Client.putObject(new PutObjectRequest(component.getBucket(), fileName, inputStream, objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    @Override
    public String getFileUrl(String fileName){
        return amazonS3Client.getUrl(component.getBucket(), fileName).toString();
    }

    public void removeFile(String fileName){
        DeleteObjectRequest request = new DeleteObjectRequest(component.getBucket(), fileName);
        amazonS3Client.deleteObject(request);
    }
}
