package com.morse.streaming.repository;


import com.morse.streaming.model.StreamingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class StreamingInfoRepository {
    private final RedisTemplate<String, StreamingInfo> redisTemplate;

    public void setStreamingInfo(String userIdx, StreamingInfo streamingInfo) {
        redisTemplate.opsForValue().set(userIdx, streamingInfo);
    }

    public StreamingInfo getStreamingInfo(String userIdx) {
        return redisTemplate.opsForValue().get(userIdx);
    }

    public void updateStreamingInfo(String presenterId, StreamingInfo streamingInfo) {
        redisTemplate.opsForValue().set(presenterId, streamingInfo);
    }

    public void deleteStreamingInfo(String userIdx) {
        redisTemplate.delete(userIdx);
        log.info("delete redis");
    }

}
