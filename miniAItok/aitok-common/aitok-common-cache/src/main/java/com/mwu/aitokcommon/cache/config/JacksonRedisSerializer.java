
        package com.mwu.aitokcommon.cache.config;

        import com.fasterxml.jackson.annotation.JsonAutoDetect;
        import com.fasterxml.jackson.annotation.JsonTypeInfo;
        import com.fasterxml.jackson.annotation.PropertyAccessor;
        import com.fasterxml.jackson.databind.JavaType;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
        import com.fasterxml.jackson.databind.type.TypeFactory;
        import org.springframework.data.redis.serializer.RedisSerializer;
        import org.springframework.data.redis.serializer.SerializationException;

        import java.nio.charset.Charset;
        import java.nio.charset.StandardCharsets;

        /**
         * Redis 使用 Jackson 序列化
         */
        public class JacksonRedisSerializer<T> implements RedisSerializer<T> {

            public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

            private final ObjectMapper objectMapper;
            private final Class<T> clazz;

            public JacksonRedisSerializer(Class<T> clazz) {
                super();
                this.clazz = clazz;
                this.objectMapper = new ObjectMapper();
                // 可见性设置
                this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                // 启用类型信息以支持多态（Jackson 2.10+ 推荐使用 LaissezFaireSubTypeValidator）
                this.objectMapper.activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                );
            }

            @Override
            public byte[] serialize(T t) throws SerializationException {
                if (t == null) {
                    return new byte[0];
                }
                try {
                    return objectMapper.writeValueAsBytes(t);
                } catch (Exception e) {
                    throw new SerializationException("Jackson serialize error", e);
                }
            }

            @Override
            public T deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    return objectMapper.readValue(bytes, clazz);
                } catch (Exception e) {
                    throw new SerializationException("Jackson deserialize error", e);
                }
            }

            protected JavaType getJavaType(Class<?> clazz) {
                return TypeFactory.defaultInstance().constructType(clazz);
            }
        }
