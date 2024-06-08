package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("初始化RedisTemplate");

        RedisTemplate redisTemplate = new RedisTemplate();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // 设置key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置value序列化器
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        return redisTemplate;
    }
}
