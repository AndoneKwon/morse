package com.morse.streaming.exception;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class TokenException extends RuntimeException {
    private WebSocketSession socketSession;
    private String id;

    TokenException() {
        super();
    }

    public TokenException(WebSocketSession socketSession, String message, String id) {
        super(message);
        this.socketSession = socketSession;
        this.id = id;
    }
}
