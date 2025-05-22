//package com.deardream.deardream_be.global.util;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//public class RedisUtil {
//
//    private final StringRedisTemplate redisTemplate;
//
//    public void setData(String key, String value) {
//        redisTemplate.opsForValue().set(key, value);
//    }
//
//    public void setDataExpire(String key, String value, long duration) {
//        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.MILLISECONDS);
//    }
//
//    public String getData(String key) {
//        return redisTemplate.opsForValue().get(key);
//    }
//
//    public void deleteData(String key) {
//        redisTemplate.delete(key);
//    }
//}
