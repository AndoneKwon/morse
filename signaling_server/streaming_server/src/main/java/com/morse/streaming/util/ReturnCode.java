package com.morse.streaming.util;

import org.springframework.stereotype.Component;

@Component
public class ReturnCode {
    public int SUCCESS = 200;
    public int FAIL = 500;
    public int NOAUTHOR = 401;
    public int NOTFOUND = 404;
}
