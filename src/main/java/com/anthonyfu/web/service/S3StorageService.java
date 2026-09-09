package com.anthonyfu.web.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anthonyfu.web.config.S3Properties;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Properties s3Properties;

    /**
     * 專門負責將 InputStream 串流上傳至 S3
     */
    public void uploadStream(String s3Key, InputStream inputStream, long contentLength) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        System.out.println("✅ 成功上傳至 S3: " + s3Key);
    }

    public String getBucketName() {
        return s3Properties.getBucketName();
    }
}
