package com.mwu.aitokstarter.file.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 。proxyBeanMethods = false 参数表示在创建 Bean 时不使用代理模式，这样可以提高性能，适用于没有依赖关系的 Bean。
//@Configuration()
@AutoConfiguration
/*

该注解的作用是将 MinioProperties 配置类加载到 Spring 容器中。MinioProperties
是一个 POJO 类，用于绑定外部配置文件（如 application.yml 或 application.properties）中以 minio 为前缀
的属性。通过这个注解，Spring 会自动将配置文件中的相关属性映射到 MinioProperties 类的字段中。
此注解确保只有在配置文件中存在 minio.endpoint 属性时，当前配置类才会生效。这是一种条件化配置的方式，
用于避免在未配置相关属性时加载不必要的 Bean，从而提高应用的灵活性和性能。
 */
@EnableConfigurationProperties(MinioProperties.class)
//@ConditionalOnProperty(prefix = "minio", name = "endpoint")
//@ConditionalOnClass(MinioClient.class)
public class MinioConfig {

    @Bean

    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnBean(MinioClient.class)
    @ConditionalOnMissingBean(MinioBucketInitializer.class)

    public MinioBucketInitializer minioBucketInitializer(MinioClient minioClient,
                                                         @Value("${minio.bucket}") String bucketName) {
        return new MinioBucketInitializer(minioClient, bucketName);
    }

}
