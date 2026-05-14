// package com.mycrewsoft.app.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.StringRedisSerializer;

// @Configuration
// public class RedisConfig {

//     /**
//      * String 타입 Key-Value 를 처리하는 RedisTemplate 빈.
//      * StringRedisSerializer 로 교체하여 Redis 에 저장된 값을 사람이 읽을 수 있게 한다.
//      */
//     @Bean
//     public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
//         RedisTemplate<String, String> template = new RedisTemplate<>();
//         template.setConnectionFactory(factory);
//         StringRedisSerializer serializer = new StringRedisSerializer();
//         template.setKeySerializer(serializer);
//         template.setValueSerializer(serializer);
//         template.setHashKeySerializer(serializer);
//         template.setHashValueSerializer(serializer);
//         return template;
//     }
// }