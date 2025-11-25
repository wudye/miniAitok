package com.mwu.aitokstarter.file.service;


import com.mwu.aitokstarter.file.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// TODO : after upload file return file url
// TODO : add file metadata support, if is vidoe or big file should support chunk upload


@EnableConfigurationProperties(MinioConfig.class)
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


    public String multipartUploadVideoFile(MultipartFile file) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 1. 校验视频类型（根据你项目实际工具类替换）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video")) {
            throw new IllegalArgumentException("仅支持视频文件上传");
        }


        String objectName = file.getOriginalFilename();
        if (objectName == null || objectName.trim().isEmpty()) {
            objectName = java.util.UUID.randomUUID().toString();
        }

        String uploadId = null;
        List<String> parts = new ArrayList<>();


        // S3 最小分片 5MB，最大 part 数 10000，这里选 10MB 并按需放大确保不超过 10000 片
        long fileSize = file.getSize();
        long minPart = 5L * 1024 * 1024;
        long partSize = Math.max(10L * 1024 * 1024, (long) Math.ceil((double) fileSize / 10_000));
        partSize = Math.max(partSize, minPart);
        try (InputStream in = file.getInputStream()) {
            long uploaded = 0;
            int partNumber = 1;
            while (uploaded < fileSize) {
                long cur = Math.min(partSize, fileSize - uploaded);
                String partName = objectName + ".part." + partNumber;
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(partName)
                                .stream(in, cur, -1)
                                .contentType(contentType)
                                .build()
                );
                parts.add(partName);
                uploaded += cur;
                partNumber++;
            }

            List<ComposeSource> sources = new ArrayList<>();
            for (String part : parts) {
                sources.add(ComposeSource.builder().bucket(bucketName).object(part).build());
            }
            minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .sources(sources)
                            .build()
            );

            return getObjectUrl(objectName);
        } catch (Exception ex) {
            for (String part : parts) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(String.valueOf(part))
                                .build()
                );
            }
            throw ex;
        }



    }

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




    public String uploadFile(MultipartFile file) throws Exception {
        String objectName = file.getOriginalFilename();
        if (objectName == null || objectName.trim().isEmpty()) {
            objectName = java.util.UUID.randomUUID().toString();
        }
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (MinioException e) {
            throw new Exception("文件上传失败: " + e.getMessage(), e);
        }
        return getObjectUrl(objectName);
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
