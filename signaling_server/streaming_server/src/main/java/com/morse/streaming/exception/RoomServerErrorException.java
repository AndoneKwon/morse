package com.morse.streaming.exception;

import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class RoomServerErrorException extends RuntimeException {
    private WebSocketSession socketSession;
    private String id;

    public RoomServerErrorException(String message) {
        super(message);
    }

    public RoomServerErrorException(WebSocketSession session, String message, String id) {
        super(message);
        this.socketSession = session;
        this.id = id;
    }
}
