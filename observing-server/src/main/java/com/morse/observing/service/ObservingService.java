package com.morse.observing.service;

import com.morse.observing.dto.request.StopCommunicationRequestDto;
import com.morse.observing.model.StreamingInfo;
import com.morse.observing.repository.FailCountRepository;
import com.morse.observing.utils.SetHttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.kurento.client.*;
import org.kurento.commons.exception.KurentoException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObservingService {
    private KurentoClient kurento = KurentoClient.create("ws://117.17.196.61:8888/kurento");
    private final RedisTemplate<String, StreamingInfo> redisTemplate;
    private final SetHttpRequestUtils httpRequestUtils;
    private final FailCountRepository failCountRepository;

    @Scheduled(fixedDelay = 2000, initialDelay = 1000)
    public void startObserving() {
        log.info("Observing");
        Set<String> keys = redisTemplate.keys("*");
        HashMap<String, StreamingInfo> streamingInfoMap = new HashMap<>();
        for(String presenterIdx : keys) {
            StreamingInfo streamingInfo = redisTemplate.opsForValue().get(presenterIdx);
            streamingInfoMap.put(presenterIdx, streamingInfo);
        }
        for(String presenter : streamingInfoMap.keySet()){
            try {
                WebRtcEndpoint webRtcEndpoint = kurento.getById(streamingInfoMap.get(presenter)
                        .getWebRtcEndPointId(), WebRtcEndpoint.class);

                Map<String, Stats> status = webRtcEndpoint.getStats();
                if (status.size() != 6) {
                    if(!failCountRepository.checkContain(presenter)) {
                        failCountRepository.addFail(presenter);
                    } else if(failCountRepository.getFail(presenter)<5) {
                        failCountRepository.increaseFail(presenter);
                        log.info(Integer.toString(failCountRepository.getFail(presenter)));
                    } else {
                        Mono<Object> response = httpRequestUtils.setHttpRequest(
                                StopCommunicationRequestDto.builder().
                                        presenterIdx(presenter).build());

                        Object object = response.block();

                        log.info("Release Endpoint");

                        continue;
                    }
                } else {
                    if(failCountRepository.checkContain(presenter)) {
                        failCountRepository.deleteFail(presenter);
                    }
                }
            } catch (KurentoException | SSLException kmse) {
                log.info("{} {} Endpoint is Already released",this.getClass(), kmse);
            }
        }
    }
}
