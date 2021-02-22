package com.morse.observing.dto.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class Message {
    String timestamp;
    private String message;
    private Object data;

    private static final String DEFAULT_KEY = "result";

    public Message() {
        this.timestamp = convertDateFormat(LocalDateTime.now());
    }

    public Message(String message) {
        this.timestamp = convertDateFormat(LocalDateTime.now());
        this.message = message;
    }

    public Message(Object result) {
        this.timestamp = convertDateFormat(LocalDateTime.now());
        this.message = "success";
        this.data = result;
    }

    public Message(Object result, String message) {
        this.timestamp = convertDateFormat(LocalDateTime.now());
        this.message = message;
        this.data = result;
    }

    private String convertDateFormat(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

}
