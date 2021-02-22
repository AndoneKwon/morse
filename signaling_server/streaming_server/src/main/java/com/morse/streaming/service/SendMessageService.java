package com.morse.streaming.service;

import com.google.gson.JsonObject;
import com.morse.streaming.dto.message.Message;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.enums.SuccessResponseEnum;
import com.morse.streaming.util.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMessageService {
    private final ResponseMessage responseMessage;

    public void sendMsgToPresenter(WebSocketSession session, String sdpAnswer, Integer roomIdx) {
        JsonObject response = new JsonObject();
        try {
            response.addProperty("id", SuccessResponseEnum.PresentConnectionSuccess.getMessage());
            response.addProperty("response", responseMessage.ACCEPT);
            response.addProperty("sdpAnswer", sdpAnswer);
            response.addProperty("roomIdx", roomIdx);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("{} {}", this.getClass(), e);
        }
    }

    public void sendMsgToViewer(WebSocketSession session, String sdpAnswer, Message message) {
        JsonObject response = new JsonObject();
        Object object = message.getData();
        try {
            response.addProperty("id", SuccessResponseEnum.ViewerConnectionSuccess.getMessage());
            response.addProperty("response", responseMessage.ACCEPT);
            response.add("data", JsonUtils.toJsonObject(object));
            response.addProperty("sdpAnswer", sdpAnswer);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("{} {}", this.getClass(), e);
        }
    }

    public void sendStopCommunication(WebSocketSession session) {
        JsonObject response = new JsonObject();

        try {
            response.addProperty("id", "stopCommunication");
            response.addProperty("message", "방송이 종료 되었습니다.");
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("{} {}", this.getClass(), e);
        }
    }

    public void sendMsgError(WebSocketSession session, String message) {
        JsonObject response = new JsonObject();

        try {
            response.addProperty("id", ErrorResponseEnum.ServerException.getMessage());
            response.addProperty("response", responseMessage.REJECTED);
            response.addProperty("message", message);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("{} {}", this.getClass(), e);
        }

    }

    public void sendMsgError(WebSocketSession session, String message, String exceptionId) {
        JsonObject response = new JsonObject();

        try {
            response.addProperty("id", exceptionId);
            response.addProperty("response", responseMessage.REJECTED);
            response.addProperty("message", message);
            session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
            log.error("{} {}", this.getClass(), e);
        }

    }

}
