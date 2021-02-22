package com.morse.streaming.enums;

public enum RequestUriEnum {
    OpenRoom("/live/open"),
    JoinRoom("/live/join"),
    CloseRoom("/live/close"),
    LeaveRoom("/live/leave");

    final private String message;

    RequestUriEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
