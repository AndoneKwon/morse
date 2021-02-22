package com.morse.streaming.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampUtils {
	private static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSS-";
	private static final DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

	private TimestampUtils() {
	}

	public static String getNow() {
		return convertDateFormat(LocalDateTime.now());
	}
	
	public static String convertDateFormat(LocalDateTime time) {
        return time.format(dateTimeformatter);
    }
}
