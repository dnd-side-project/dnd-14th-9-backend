package com.example.gak.global.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.gak.global.configuration.AmazonConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

    private final AmazonS3 amazonS3;
    private final AmazonConfig amazonConfig;

    public String uploadFile(String keyName, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
        } catch (IOException e) {
            log.error("S3 업로드 실패: keyName={}", keyName, e);
            throw new RuntimeException("S3 업로드 오류 발생", e);
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    public void deleteFile(String fileUrl) {
        try {
            String keyName = URI.create(fileUrl).getPath().substring(1);
            amazonS3.deleteObject(amazonConfig.getBucket(), keyName);
            log.info("Deleted S3 file: {}", keyName);

        } catch (Exception e) {
            log.error("S3 delete failed for file: {}", fileUrl, e);
            throw new RuntimeException("S3 삭제 오류 발생", e);
        }
    }

    public String generateProfileKeyName() {
        String uuid = UUID.randomUUID().toString();
        return amazonConfig.getProfilePath() + '/' + uuid;
    }

    public String generateSessionThumbnailKeyName() {
        String uuid = UUID.randomUUID().toString();
        return amazonConfig.getSessionThumbnailPath() + '/' + uuid;
    }
}
