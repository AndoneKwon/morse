package com.morse.streaming.exception;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class HttpRequestErrorException extends RuntimeException{
    private WebSocketSession socketSession;
    private String id;

    HttpRequestErrorException() {
        super();
    }

    public HttpRequestErrorException(WebSocketSession session, String message, String id) {
        super(message);
        this.socketSession = session;
        this.id = id;
    }
/*
    @Override
    public synchronized Throwable fillInStackTrace(){
        return this;
    }

 */
}
