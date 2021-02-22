package com.morse.streaming.exception;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class NotStartedException extends RuntimeException {
    private WebSocketSession socketSession;
    private String id;

    public NotStartedException() {
        super();
    }

    public NotStartedException(WebSocketSession session, String message, String id) {
        super(message);
        this.socketSession = session;
        this.id = id;
    }

    @Override
    public synchronized Throwable fillInStackTrace(){
        return this;
    }
}
