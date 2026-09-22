package com.team202ok.demo.domain.message.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequiredArgsConstructor
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    // 저장
    @PostMapping("/redis/set")
    public String set(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "저장됨: " + key + " = " + value;
    }

    // 조회
    @GetMapping("/redis/get")
    public String get(@RequestParam String key) {
        return "값: " + redisTemplate.opsForValue().get(key);
    }
}
