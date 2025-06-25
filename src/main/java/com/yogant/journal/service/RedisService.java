package com.yogant.journal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisService {

    private RedisTemplate redisTemplate;


    public RedisService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T getData(String key, Class<T> responseClass) {

        try {
            Object object = redisTemplate.opsForValue().get(key);
            ObjectMapper mapper = new ObjectMapper();
            if (object != null) return mapper.readValue(object.toString(), responseClass);
        } catch (Exception e) {
            log.error("Error retrieving cache from redis for weather :{}", e.getMessage(), e);
            return null;
        }
        return null;
    }


    public void setData(String key, Object o, long ttl) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonValue = mapper.writeValueAsString(o);
            redisTemplate.opsForValue().set(key, jsonValue, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error saving data in redis for caching :{}", e.getMessage(), e);
        }
    }
}
