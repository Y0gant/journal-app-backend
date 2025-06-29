package com.yogant.journal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class RedisService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final RedisTemplate<String, Object> redisTemplate;

    public <T> T getData(String key, Class<T> responseClass) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }

        try {
            Object object = redisTemplate.opsForValue().get(key);
            if (object == null || object.toString().isBlank()) return null;
            return mapper.readValue(object.toString(), responseClass);
        } catch (Exception e) {
            log.error("Error retrieving Redis cache for key '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

    public void setData(String key, Object data, long ttlSeconds) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        try {
            String jsonValue = mapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonValue, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error caching data in Redis for key '{}': {}", key, e.getMessage(), e);
        }
    }
}
