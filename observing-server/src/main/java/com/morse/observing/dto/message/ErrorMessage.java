package com.morse.observing.dto.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ErrorMessage {
    String timestamp;
    int status;
    String message;
    String path;

    public ErrorMessage() { }

    public ErrorMessage(String errorMessage, int status, String path) {
        this.timestamp = convertDateFormat(LocalDateTime.now());
        this.status = status;
        this.message = errorMessage;
        this.path = path;
    }

    public ErrorMessage(String errorMessage, String path) {
        this.timestamp = convertDateFormat(LocalDateTime.now());
        this.message = errorMessage;
        this.path = path;
    }

    private String convertDateFormat(LocalDateTime now) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
