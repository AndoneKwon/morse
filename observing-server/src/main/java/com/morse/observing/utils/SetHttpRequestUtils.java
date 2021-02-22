package com.morse.observing.utils;

import com.morse.observing.dto.message.ErrorMessage;
import com.morse.observing.dto.message.Message;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Slf4j
@Component
public class SetHttpRequestUtils {
    public Mono<Object> setHttpRequest(Object object) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create().secure(t->t.sslContext(sslContext));
        WebClient webClient =
                WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                        .baseUrl("https://localhost:8888/release").build();

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(res -> {
                    if (res.statusCode().equals(HttpStatus.OK)) {
                        log.info("Status OK");
                        return res.bodyToMono(Message.class);
                    } else {
                        return res.bodyToMono(ErrorMessage.class);
                    }
                });
    }
}
