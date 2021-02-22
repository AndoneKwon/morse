package com.morse.observing;

import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@EnableWebSocket
@EnableScheduling
@SpringBootApplication
public class ObservingApplication {
    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create("ws://117.17.196.61:8888/kurento");
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        return container;
    }

    public static void main(String[] args) {
        SpringApplication.run(ObservingApplication.class, args);
    }

}
