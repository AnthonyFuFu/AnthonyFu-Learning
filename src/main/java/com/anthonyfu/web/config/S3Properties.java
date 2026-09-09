package com.anthonyfu.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {

    private String region;
    private String bucketName;
    private String accessKey;
    private String secretKey;
//    private String sessionToken; // 測試用
    
}
