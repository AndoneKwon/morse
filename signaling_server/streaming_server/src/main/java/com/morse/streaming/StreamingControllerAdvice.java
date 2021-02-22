package com.morse.streaming;

import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.exception.*;
import com.morse.streaming.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class StreamingControllerAdvice {
    private final SendMessageService sendMessageService;

    @ExceptionHandler(value = {AlreadyStartedException.class})
    public void handleAlreadyStartedException(AlreadyStartedException alreadyStartedException) {
        log.error("{} {}", alreadyStartedException.getMessage(), alreadyStartedException);
    }

    @ExceptionHandler(value = {AlreadyJoinedException.class})
    public void handleAlreadyJoinedException(AlreadyJoinedException alreadyJoinedException) {
        log.error("{} {}", alreadyJoinedException.getMessage(), alreadyJoinedException);
    }

    @ExceptionHandler(value = {RoomServerErrorException.class})
    public void handleRoomServerErrorException(RoomServerErrorException roomServerErrorException) {
        log.error("{} {}", roomServerErrorException.getMessage(), roomServerErrorException);
    }

    @ExceptionHandler(value = {NotStartedException.class})
    public void handleNotStartedException(NotStartedException notStartedException) {
        log.error("{} {}", notStartedException.getMessage(), notStartedException);
    }

    @ExceptionHandler(value = {HttpRequestErrorException.class})
    public void handleHttpRequestErrorException(HttpRequestErrorException httpRequestErrorException) {
        log.error("{} {}", httpRequestErrorException.getMessage(), httpRequestErrorException);
    }

    @ExceptionHandler(value = {TokenException.class})
    public void handleTokenException(TokenException tokenException) {
        log.error("{} {}", tokenException.getMessage(), tokenException);
    }

    @ExceptionHandler(value = {IceCandidateException.class})
    public void handleIceCandidateException(TokenException tokenException) {
        log.error("{} {}", tokenException.getMessage(), tokenException);
    }

    @ExceptionHandler(value = {TokenNullException.class})
    public void handleTokenNullException(TokenNullException tokenNullException) {
        log.error("{} {}", tokenNullException.getMessage(), tokenNullException);
    }
}