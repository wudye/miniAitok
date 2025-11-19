package com.mwu.aitokstarter.file.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String endpoint;

}