package com.morse.streaming.enums;

public enum SuccessResponseEnum {
    PresentConnectionSuccess("presenterResponse"),
    ViewerConnectionSuccess("viewerResponse"),
    IceCandidate("iceCandidate");

    final private String message;

    SuccessResponseEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}