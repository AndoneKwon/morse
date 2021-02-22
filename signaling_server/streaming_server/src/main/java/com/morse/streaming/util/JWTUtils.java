package com.morse.streaming.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.exception.TokenException;
import com.morse.streaming.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class JWTUtils {
    @Value("${jwt.secret}")
    private String KEY;

    private final SendMessageService sendMessageService;
    private final ResponseMessage responseMessage;

    JWTVerifier jwtVerifier;

    @PostConstruct
    protected void init() {
        jwtVerifier = JWT.require(Algorithm.HMAC256(KEY)).build();
    }

    public boolean checkToken(String tokenHeader) {
        try {
            jwtVerifier.verify(tokenHeader);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void validateToken(String tokenHeader) {
        log.info("token header : " + tokenHeader);
        try {
            jwtVerifier.verify(tokenHeader);
            log.info("Token validate");
        } catch (TokenExpiredException te) {
            log.error(te.getMessage());
        } catch (SignatureVerificationException sve) {
            log.error(sve.getMessage());
        } catch (JWTDecodeException jde) {
            log.error(jde.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public String decodeTokenToId(String token, WebSocketSession session) {
        try {
            Long a = JWT.decode(token).getClaim("user_idx").asLong();
            return a.toString();
        } catch (JWTDecodeException jde) {
            log.error("{} {}", this.getClass(), jde);

            throw new TokenException(session, responseMessage.AUTHORIZATION_EXCEPTION,
                    ErrorResponseEnum.AuthorizationException.getMessage());
        }
    }
}
