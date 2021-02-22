package com.morse.streaming.enums;

import org.springframework.stereotype.Component;

public enum ErrorResponseEnum {
    UserException("userException"),
    ServerException("serverException"),
    AuthorizationException("tokenException");
    final private String message;

    ErrorResponseEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
