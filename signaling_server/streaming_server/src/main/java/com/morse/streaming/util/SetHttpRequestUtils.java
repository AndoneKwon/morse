package com.morse.streaming.util;

import com.morse.streaming.dto.message.ErrorMessage;
import com.morse.streaming.dto.message.Message;
import com.morse.streaming.dto.request.CreateRoomRequestDto;
import com.morse.streaming.dto.request.JoinRoomRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SetHttpRequestUtils {
    final String baseUrlRoom = "http://downsups.onstove.com:8005/room";
    WebClient webClient = WebClient.builder().build();

    public Mono<?> setHttpRequest(String token, String uri, Object object) {
        return webClient.mutate().baseUrl(baseUrlRoom).build().post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("token", token)
                .bodyValue(object)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(res -> {
                    if (res.statusCode().equals(HttpStatus.OK)) {
                        return res.bodyToMono(Message.class);
                    } else {
                        return res.bodyToMono(ErrorMessage.class);
                    }
                });

    }
    
    public Mono<?> setHttpThumbRequest(String baseUrl, String uri, Object object) {
        return webClient.mutate().baseUrl(baseUrl).build().post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(res -> {
                    if (res.statusCode().equals(HttpStatus.OK)) {
                        return res.bodyToMono(Message.class);
                    } else {
                        return res.bodyToMono(ErrorMessage.class);
                    }
                });
    }
}
