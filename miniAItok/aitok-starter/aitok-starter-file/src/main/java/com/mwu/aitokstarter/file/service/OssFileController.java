package com.mwu.aitokstarter.file.service;


//import com.mwu.common.mode.mongo.FileNameMapping;
//import com.mwu.common.repository.FileNameMappingRepository;
//import com.mwu.core.util.info.Result;
//import io.minio.*;
//import io.minio.http.Method;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/oss/files")
//
//@Slf4j
public class OssFileController {

//    private final MinioClient minioClient;
//    private final FileNameMappingRepository fileNameMappingRepository;
//    private final String bucketName;
//
//    public OssFileController(MinioClient minioClient, FileNameMappingRepository fileNameMappingRepository,
//                             @Value("${minio.bucketName}") String bucketName) {
//        this.minioClient = minioClient;
//        this.fileNameMappingRepository = fileNameMappingRepository;
//        this.bucketName = bucketName;
//    }
//    @GetMapping("/test")
//    public String test() {
//        System.out.println("test");
//        return "test successful";
//    }
//
//    /**
//     * upload file
//     */
//    @PostMapping("/upload")
//    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try{
//
//            /*
//            boolean exists = minioClient.bucketExists(
//                    BucketExistsArgs.builder().bucket(bucketName).build());
//            if (exists) {
//                log.info("MinIO bucket '{}' already exists", bucketName);
//            } else {
//                log.info("Creating MinIO bucket '{}'", bucketName);
//                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
//                log.info("Bucket '{}' created", bucketName);
//            }
//
//             */
//            System.out.println("upload file");
//            String fileMd5 = DigestUtils.md5Hex(file.getInputStream());
//
//            Optional<FileNameMapping> existingMapping = fileNameMappingRepository.findFirstByFileMd5(fileMd5);
//            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//            if (existingMapping.isPresent()) {
//                uniqueFileName = existingMapping.get().getUniqueFileName();
//                log.info("File already exists: {}", file.getOriginalFilename());
//            } else {
//                minioClient.putObject(
//                        PutObjectArgs.builder()
//                                .bucket(bucketName)
//                                .object(uniqueFileName)
//                                .stream(file.getInputStream(), file.getSize(), -1)
//                                .contentType(file.getContentType())
//                                .build()
//                );
//            }
//            System.out.println("after put object");
//            String id = UUID.randomUUID().toString().replace("-", "");
//            FileNameMapping fileNameMapping = new FileNameMapping();
//            fileNameMapping.setId(id);
//            fileNameMapping.setOriginalFileName(file.getOriginalFilename());
//            fileNameMapping.setUniqueFileName(uniqueFileName);
//            fileNameMapping.setFileMd5(fileMd5);
//            fileNameMapping.setCreateTime(LocalDateTime.now());
//            fileNameMapping.setUpdateTime(LocalDateTime.now());
//            fileNameMappingRepository.save(fileNameMapping);
//            log.info("文件上传成功: {}", fileNameMapping.getId());
//            return Result.ok(fileNameMapping.getId());
//        } catch (Exception e) {
//            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
//            return Result.fail("文件上传失败: " + e.getMessage());
//        }
//
//    }
//    /**
//     * 82b25d2cbd5447168b745b17460d974e
//     * 82b25d2cbd5447168b745b17460d974e
//     * 处理文件下载请求
//     * 根据请求参数 responseType 的不同，可以选择直接返回文件内容（以二进制形式）或返回文件的下载 URL。
//     *
//     * @param fileId       查找文件的唯一标识。
//     * @param responseType 下载响应类型，默认为 "url"。可选值为 "binary"（二进制数据响应）和 "url"（文件下载 URL 响应）。
//     * @return 根据 responseType 的不同，返回文件内容或文件下载 URL。如果文件不存在，返回错误信息。
//     */
//    @GetMapping("/download/{fileId}")
//    public Result<?> downloadFile(@PathVariable String fileId,
//                                  @RequestParam(value = "responseType", defaultValue = "url") String responseType) {
//        String originalFileName = null;
//        try {
//            // 从 MongoDB 获取唯一文件名
//            Optional<FileNameMapping> fileNameMapping = fileNameMappingRepository.findById(fileId);
//            if (fileNameMapping.isEmpty()) {
//                log.warn("File not found: {}", fileId);
//                return Result.fail("File not found");
//            }
//            originalFileName = fileNameMapping.get().getOriginalFileName();
//
//            if ("binary".equalsIgnoreCase(responseType)) {
//                // 返回二进制数据
//                InputStream inputStream = minioClient.getObject(
//                        GetObjectArgs.builder()
//                                .bucket(bucketName)
//                                .object(fileNameMapping.get().getUniqueFileName())
//                                .build()
//                );
//
//                byte[] content = inputStream.readAllBytes();
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//                headers.setContentDispositionFormData("attachment", originalFileName);
//
//                log.info("File downloaded as binary: {}", originalFileName);
//                return Result.ok(content);
//            } else {
//                // 返回文件的 URL
//                String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
//                        .method(Method.GET)
//                        .bucket(bucketName)
//                        .object(fileNameMapping.get().getUniqueFileName())
//                        .expiry(60 * 60 * 24)
//                        .build()
//                );
//                log.info("File URL generated: {}", presignedObjectUrl);
//                return Result.ok(presignedObjectUrl);
//            }
//        } catch (Exception e) {
//            log.error("文件下载失败: {}", originalFileName, e);
//            return Result.fail("文件下载失败: " + e.getMessage());
//        }
//    }
}

