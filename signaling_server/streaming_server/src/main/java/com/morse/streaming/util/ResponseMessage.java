package com.morse.streaming.util;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ResponseMessage {
    /* Response Code */
    public final String ACCEPT = "accepted";
    public final String REJECTED = "reject";

    /* Response Viewing Error Message */
    public final String VIEWER_NOT_STARTED = "not started yet";
    public final String VIEWER_ALREADY_VIEWING = "you already watch this streaming";

    /* Response PRESENT Error Message */
    public final String PRESENT_NO_ROOM = "Room is not created";
    public final String PRESENT_ALREADY_STARTED = "you already start this present";

    /* Exception Message */
    public final String IO_EXCEPTION_MESSAGE = "IOException";
    public final String NULL_POINT_EXCEPTION_MESSAGE = "Null Point Exception";
    public final String UNPREDICTED_EXCEPTION = "Unpredicted Exception";
    public final String MAKE_ROOM_EXCEPTION = "Make Room Exception";
    public final String AUTHORIZATION_EXCEPTION = "Authorization Exception";
    public final String NULL_TOKEN = "Token is Null";

}
