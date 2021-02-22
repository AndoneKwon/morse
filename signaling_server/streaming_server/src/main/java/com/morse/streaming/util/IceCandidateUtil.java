package com.morse.streaming.util;

import com.google.gson.JsonObject;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.enums.SuccessResponseEnum;
import com.morse.streaming.service.SendMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class IceCandidateUtil {
    private final SendMessageService sendMessageService;
    private final ResponseMessage responseMessage;

    /* add Candidate Listener add */
    public void addIceCandidateFoundListener(WebSocketSession session, WebRtcEndpoint endpoint) {
        endpoint.addIceCandidateFoundListener(event -> {
            JsonObject response = new JsonObject();
            response.addProperty("id", SuccessResponseEnum.IceCandidate.getMessage());
            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(response.toString()));
                }
            } catch (IOException e) {
                log.error("IOException {}", e);
            } catch (NullPointerException nullPointerException) {
                log.error("{} {}", this.getClass(), nullPointerException);

                sendMessageService.sendMsgError(session, responseMessage.NULL_POINT_EXCEPTION_MESSAGE,
                        ErrorResponseEnum.UserException.getMessage());
            } catch (Exception unKnownException) {
                log.error("{} {}", this.getClass(), unKnownException);

                sendMessageService.sendMsgError(session, responseMessage.UNPREDICTED_EXCEPTION,
                        ErrorResponseEnum.ServerException.getMessage());
            }
        });
    }
}
