package com.morse.streaming.handler;

import java.io.IOException;

import com.morse.streaming.dto.request.LeaveRoomRequestDto;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.enums.RequestUriEnum;
import com.morse.streaming.exception.TokenNullException;
import com.morse.streaming.model.SessionInfo;
import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.repository.SessionRepository;
import com.morse.streaming.repository.StreamingInfoRepository;
import com.morse.streaming.service.*;

import com.morse.streaming.util.GetMediaServerUtil;
import com.morse.streaming.util.JWTUtils;
import com.morse.streaming.util.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingWebsocketHandler extends TextWebSocketHandler {
    private static final String RECORDER_FILE_PATH = "file:///tmp/HelloWorldRecorded.webm";
    private static final Gson gson = new GsonBuilder().create();
    private final PresenterService presenterService;
    private final JoinService joinService;
    private final IceCandidateService iceCandidateService;
    private final SessionRepository sessionRepository;
    private final SendMessageService sendMessageService;
    private final ResponseMessage responseMessage;
    private final RollbackService rollbackService;


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String isPing = message.getPayload();
        if(isPing.equals("ping")){
            //log.info("ping");
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        String token = null;
        if(jsonMessage.getAsJsonPrimitive("token").isJsonNull()){
            sendMessageService.sendMsgError(session, responseMessage.NULL_TOKEN,
                    ErrorResponseEnum.UserException.getMessage());
            throw new TokenNullException(responseMessage.NULL_TOKEN);
        } else {
            token = jsonMessage.getAsJsonPrimitive("token").getAsString();
        }

        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);
        switch (jsonMessage.get("id").getAsString()) {
            case "presenter":
                presenterService.presenterConnection(session, jsonMessage, token);
                break;
            case "viewer":
                joinService.joinRoom(session, jsonMessage, token);
                break;
            case "onIceCandidate": {
                iceCandidateService.iceCandidate(session, jsonMessage, token);
                break;
            }
            case "stopPresenter":
                presenterService.stopPresenter(session, jsonMessage);
                break;
            case "stopViewer":
                joinService.stopViewer(session, jsonMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public void handlePongMessage(WebSocketSession session, PongMessage pongMessage) {
        System.out.println(pongMessage.toString());
    }


    private void handleErrorResponse(Throwable throwable, WebSocketSession session, String responseId)
            throws IOException {
        log.error(throwable.getMessage(), throwable);
        JsonObject response = new JsonObject();
        response.addProperty("id", responseId);
        response.addProperty("response", "rejected");
        response.addProperty("message", throwable.getMessage());
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info(status.toString());

        SessionInfo sessionInfo = sessionRepository.getSessionInfo(session.getId());
        if(sessionInfo==null) return;
        if(sessionInfo.isPresenter()){
            log.info("Presenter Stop");
            rollbackService.stopPresenter(sessionInfo);
        } else {
            rollbackService.stopViewer(session.getId(), sessionInfo);
        }
        session.close();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new PingMessage());
    }


}

