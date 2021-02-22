package com.morse.streaming.exception;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class AlreadyJoinedException extends RuntimeException {
    private WebSocketSession socketSession;
    private String message;
    private String id;

    public AlreadyJoinedException() {
        super();
    }

    public AlreadyJoinedException(WebSocketSession session, String message, String id) {
        this.socketSession = session;
        this.message = message;
        this.id = id;
    }

    @Override
    public synchronized Throwable fillInStackTrace(){
        return this;
    }
}
