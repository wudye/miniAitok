package com.mwu.aitokstarter.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
/*

在Spring Boot应用完全启动后执行
保证所有Bean都已初始化完成
适合执行初始化任务
MinioClient Bean已经创建完毕后
自动检查并创建所需的存储桶
 */
@Slf4j
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioBucketInitializer(MinioClient minioClient,
                                  @Value("${minio.bucket}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            System.out.println("Initializing MinIO bucket: " + bucketName);
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (exists) {
                log.info("MinIO bucket '{}' already exists", bucketName);
            } else {
                log.info("Creating MinIO bucket '{}'", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            System.out.println("Bucket initialization completed");
        } catch (Exception e) {
            log.error("Failed to ensure MinIO bucket '{}': {}", bucketName, e.getMessage(), e);
        }
    }
}
