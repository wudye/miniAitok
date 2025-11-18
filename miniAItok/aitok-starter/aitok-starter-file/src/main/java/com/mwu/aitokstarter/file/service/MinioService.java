package com.mwu.aitokstarter.file.service;


import com.mwu.aitokstarter.file.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// TODO : after upload file return file url
// TODO : add file metadata support, if is vidoe or big file should support chunk upload


@EnableConfigurationProperties(MinioProperties.class)
//此注解用于将 MinioConfig 配置类导入到当前上下文中。通过这种方式，MinioConfig 中定义的 Bean 和配置会被加载到 Spring 容器中，从而在当前类中可用。
@Import(MinioConfig.class)

public class MinioService {


    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.public-read:true}")
    private boolean publicRead;

    @Value("${minio.endpoint}")
    private String endpoint;


    public void uploadObject(String objectName, String localFilePath, String contentType) throws Exception {
        UploadObjectArgs args = UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(localFilePath)
                .contentType(contentType)
                .build();
        minioClient.uploadObject(args);
    }

    public String getObjectUrl(String objectName) {
        if (publicRead) {
            // 公共桶：直接拼接 S3 风格路径
            return endpoint + "/" + bucketName + "/" + objectName;
        }
        // 私有桶：生成短期预签名 URL
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("生成 MinIO 预签名 URL 失败", e);
        }
    }



    public void uploadFile(MultipartFile file) throws Exception {
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getOriginalFilename())
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("文件上传失败: " + e.getMessage());
        }
    }

    public InputStream downloadFile(String objectName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("文件下载失败: " + e.getMessage());
        }
    }

    public void deleteFile(String objectName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("文件删除失败: " + e.getMessage());
        }
    }

    /*

    在你的 fileExists 方法中，如果 minioClient.statObject 抛出异常，并且异常信息包含 "Not Found"，说明文件不存在；
如果没有异常或异常不是 "Not Found"，则认为文件存在或发生了其他错误。
所以，只有捕获到 "Not Found" 异常时才表示没有该文件。
     */


    public boolean fileExists(String objectName) throws Exception {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new Exception("文件状态查询失败: " + e.getMessage());
        } catch (MinioException e) {
            throw new Exception("文件状态查询失败: " + e.getMessage());
        }
    }

    public List<String> listFiles() throws Exception {
        try {
            List<String> fileNames = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            for (Result<Item> result : results) {
                fileNames.add(result.get().objectName());
            }
            return fileNames;
        } catch (MinioException e) {
            throw new Exception("获取文件列表失败: " + e.getMessage());
        }
    }

    public Map<String, String> getFileMetadata(String objectName) throws Exception {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            Map<String, String> metadata = new HashMap<>();
            metadata.put("size", String.valueOf(stat.size()));
            metadata.put("contentType", stat.contentType());
            metadata.put("lastModified", stat.lastModified().toString());
            return metadata;
        } catch (MinioException e) {
            throw new Exception("获取文件元数据失败: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(String objectName, Duration expiry) throws Exception {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry((int) expiry.toSeconds())
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("生成预签名 URL 失败: " + e.getMessage());
        }
    }
}
