package com.morse.streaming.service;

import com.google.gson.JsonObject;
import com.morse.streaming.dto.message.ErrorMessage;
import com.morse.streaming.dto.message.Message;
import com.morse.streaming.dto.request.CreateRoomRequestDto;
import com.morse.streaming.dto.request.LeaveRoomRequestDto;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.enums.RequestUriEnum;
import com.morse.streaming.exception.*;
import com.morse.streaming.model.SessionInfo;
import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.repository.*;
import com.morse.streaming.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenterService {
    private final KurentoClient kurento;
    private final ResponseMessage responseMessage;
    private final StreamingInfoRepository streamingInfoRepository;
    private final SendMessageService sendMessageService;
    private final IceCandidateUtil iceCandidateUtil;
    private final JWTUtils jwtUtils;
    private final SetHttpRequestUtils httpRequestUtils;
    private final GetMediaServerUtil getMediaServerUtil;
    private final SessionRepository sessionRepository;
    private final RecordService recordService;
    private final JoinedViewerRepository joinedViewerRepository;
    private final PresenterSessionRepository presenterSessionRepository;

    public void presenterConnection(final WebSocketSession session, JsonObject jsonMessage, String token) {
        String title = jsonMessage.getAsJsonPrimitive("title").getAsString();
        String contents = jsonMessage.getAsJsonPrimitive("contents").getAsString();
        String presenterIdx = jwtUtils.decodeTokenToId(token, session);

        sessionRepository.addSession(session.getId(), SessionInfo.builder().
                userIdx(presenterIdx).
                isPresenter(true).
                token(token).
                presenterIdx(presenterIdx).
                build());

        StreamingInfo streamingInfo = streamingInfoRepository.getStreamingInfo(presenterIdx);
        if (streamingInfo == null) {
            MediaPipeline pipeline = kurento.createMediaPipeline();
            WebRtcEndpoint presenterWebRtc = new WebRtcEndpoint.Builder(pipeline).useDataChannels().build();

            iceCandidateUtil.addIceCandidateFoundListener(session, presenterWebRtc);

            String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
            String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

            streamingInfoRepository.setStreamingInfo(presenterIdx, streamingInfo);
            Mono<?> makeRoomResponse = httpRequestUtils.setHttpRequest(token,
                    RequestUriEnum.OpenRoom.getMessage(),
                    CreateRoomRequestDto.builder().title(title).contents(contents).build());

            presenterWebRtc.setOutputBitrate(2147483647);
            presenterWebRtc.setMaxVideoSendBandwidth(5000);
            presenterWebRtc.setMaxVideoRecvBandwidth(5000);

            makeRoomResponse.subscribe(new Consumer<Object>() {
                @Override
                public void accept(Object object) {
                    System.out.println(object);
                    if (object instanceof Message) {
                        Message message = (Message)object;
                        JsonObject returnObject = JsonUtils.toJsonObject(message.getData());
                        int roomIdx = returnObject.getAsJsonPrimitive("roomIdx").getAsInt();
                        String recordLocation = returnObject.getAsJsonPrimitive("recordLocation").getAsString();
                        joinedViewerRepository.makeNewRoom(presenterIdx);
                        presenterSessionRepository.addSession(presenterIdx, session);

                        sendMessageService.sendMsgToPresenter(session, sdpAnswer, roomIdx);

                        //recordService is return StreamingInfo Object
                        presenterWebRtc.gatherCandidates();
                        streamingInfoRepository.updateStreamingInfo(presenterIdx,
                                recordService.record(pipeline, presenterWebRtc, recordLocation, roomIdx));
                        return;
                    } else {
                        ErrorMessage errorMessage = (ErrorMessage) object;
                        if (errorMessage.getStatus() == 401) {
                            sendMessageService.sendMsgError(session,
                                    responseMessage.AUTHORIZATION_EXCEPTION,
                                    ErrorResponseEnum.UserException.getMessage());
                            throw new TokenException(session,
                                    responseMessage.AUTHORIZATION_EXCEPTION,
                                    ErrorResponseEnum.AuthorizationException.getMessage());
                        }
                        sendMessageService.sendMsgError(session,
                                responseMessage.MAKE_ROOM_EXCEPTION,
                                ErrorResponseEnum.UserException.getMessage());
                        throw new RoomServerErrorException(session, errorMessage.getMessage(),
                                ErrorResponseEnum.UserException.getMessage());
                    }
                }
            });
        } else {
            sendMessageService.sendMsgError(session,
                    responseMessage.PRESENT_ALREADY_STARTED,
                    ErrorResponseEnum.UserException.getMessage());
            throw new AlreadyStartedException(session, responseMessage.PRESENT_ALREADY_STARTED,
                    ErrorResponseEnum.UserException.getMessage());
        }
        return;
    }

    public void stopPresenter(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        try {
            log.info("Try Stop Presenter");
            String token = jsonMessage.get("token").getAsString();
            String presenterIdx = jwtUtils.decodeTokenToId(token, session);

            StreamingInfo streamingInfo = streamingInfoRepository.getStreamingInfo(presenterIdx);

            Mono<?> stopRoomResponse = httpRequestUtils.setHttpRequest(
                    token, RequestUriEnum.CloseRoom.getMessage(),
                    LeaveRoomRequestDto.builder().presenterIdx(presenterIdx).build());

            Object object = stopRoomResponse.block();

            streamingInfoRepository.deleteStreamingInfo(presenterIdx);
            sessionRepository.deleteSession(session.getId());
            sendMessageService.sendStopCommunication(session);
            recordService.releaseAll(streamingInfo);Collection<WebSocketSession> viewerSessions =
                    joinedViewerRepository.getViewerCollection(presenterIdx);
            presenterSessionRepository.deleteSession(presenterIdx);
            for(WebSocketSession viewerSession : viewerSessions) {
                log.info(viewerSession.getId());
                sendMessageService.sendStopCommunication(viewerSession);
            }

            if (object == null) {
                session.close();
            } else {
                ErrorMessage errorMessage = (ErrorMessage) object;
                sendMessageService.sendMsgError(session
                        , errorMessage.getMessage()
                        , ErrorResponseEnum.ServerException.getMessage());

                throw new RoomServerErrorException(session, errorMessage.getMessage(),
                        ErrorResponseEnum.UserException.getMessage());
            }
        } catch (NullPointerException nullPointerException) {
            log.error("{} {}", this.getClass(), nullPointerException);

            sendMessageService.sendMsgError(session, responseMessage.NULL_POINT_EXCEPTION_MESSAGE,
                    ErrorResponseEnum.UserException.getMessage());
            session.close();
        } catch (Exception unKnownException) {
            log.error("{} {}", this.getClass(), unKnownException);

            sendMessageService.sendMsgError(session, responseMessage.UNPREDICTED_EXCEPTION,
                    ErrorResponseEnum.ServerException.getMessage());
            session.close();
        }
    }
}
