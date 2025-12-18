package com.mwu.aitokcommon.cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mwu.aitokcommon.cache.service.LockService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * redis引入fastjson序列化器
 */
@Configuration
public class RedisConfig {

    // 注入你在 JacksonConfig 中声明的 ObjectMapper bean
    private final ObjectMapper objectMapper;

    @Value("${spring.redis.host:192.168.80.130}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.password:123456789}")
    private String password;
    @Value("${spring.redis.database:1}")
    private int database;




    public RedisConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 使用默认 host/port（localhost:6379），可通过 application.properties 覆盖
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setDatabase(database);
        factory.setHostName(host);
        factory.setPort(port);
        factory.setPassword(password);
        return factory;
    }


    /*
    Jackson2JsonRedisSerializer
优点: 性能更好，序列化结果更紧凑
缺点: 需要明确指定类型，反序列化时必须知道确切类型
适用场景: 类型固定的缓存数据

GenericJackson2JsonRedisSerializer ⭐ 推荐
优点: 自动包含类型信息，能处理多态对象，更灵活
缺点: 序列化结果稍大（包含@class信息）
适用场景: 缓存多种类型对象，需要类型自动识别
     */

    @Bean
    @Primary
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 创建专门用于 Redis 的 ObjectMapper，不影响 HTTP 消息转换
        ObjectMapper redisObjectMapper = new ObjectMapper();

        // 设置日期时间处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        redisObjectMapper.registerModule(javaTimeModule);

        // 设置序列化配置
        redisObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        redisObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        redisObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        redisObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 激活默认类型信息，确保反序列化时能正确还原原始类型
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 使用 GenericJackson2JsonRedisSerializer 以获得更好的兼容性
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jacksonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jacksonSerializer);
        template.setDefaultSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /*

@Bean
@Primary
@SuppressWarnings({"rawtypes", "unchecked"})
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    StringRedisSerializer stringSerializer = new StringRedisSerializer();

    // 复制现有的ObjectMapper配置，不影响HTTP层
    ObjectMapper redisObjectMapper = new ObjectMapper();

    // 复制JacksonConfig中的配置
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    redisObjectMapper.registerModule(javaTimeModule);
    redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 复制字符串trim配置
    SimpleModule stringTrimModule = new SimpleModule();
    stringTrimModule.addDeserializer(String.class, new JsonDeserializer<String>() {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            return value == null ? null : value.trim();
        }
    });
    redisObjectMapper.registerModule(stringTrimModule);

    redisObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 只为Redis启用类型信息
    redisObjectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
    );

    // 选择序列化器
    GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

    template.setKeySerializer(stringSerializer);
    template.setValueSerializer(jacksonSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setHashValueSerializer(jacksonSerializer);
    template.setDefaultSerializer(jacksonSerializer);

    template.afterPropertiesSet();
    return template;
}

*/
//    @Bean
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
//        StringRedisTemplate template = new StringRedisTemplate();
//        template.setConnectionFactory(factory);
//        return template;
//    }
    @Bean
    public LockService lockService(RedissonClient redissonClient) {
        return new LockService(redissonClient);
    }
}
