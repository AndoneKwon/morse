package com.morse.streaming.service;

import com.google.gson.JsonObject;
import com.morse.streaming.dto.message.ErrorMessage;
import com.morse.streaming.dto.message.Message;
import com.morse.streaming.dto.request.JoinRoomRequestDto;
import com.morse.streaming.dto.request.LeaveRoomRequestDto;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.enums.RequestUriEnum;
import com.morse.streaming.exception.AlreadyJoinedException;
import com.morse.streaming.exception.NotStartedException;
import com.morse.streaming.exception.RoomServerErrorException;
import com.morse.streaming.exception.TokenException;
import com.morse.streaming.model.SessionInfo;
import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.repository.JoinedViewerRepository;
import com.morse.streaming.repository.SessionRepository;
import com.morse.streaming.repository.StreamingInfoRepository;
import com.morse.streaming.repository.ViewerConnectionRepository;
import com.morse.streaming.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinService {
    private final GetMediaServerUtil getMediaServerUtil;
    private final ResponseMessage responseMessage;
    private final StreamingInfoRepository streamingInfoRepository;
    private final ViewerConnectionRepository viewerConnectionRepository;
    private final IceCandidateUtil iceCandidateUtil;
    private final SendMessageService sendMessageService;
    private final SetHttpRequestUtils httpRequestUtils;
    private final JWTUtils jwtUtils;
    private final SessionRepository sessionRepository;
    private final JoinedViewerRepository joinedViewerRepository;
    private final WebClient webClient = WebClient.create("http://downsups.onstove.com:8005/room");

    public void joinRoom(final WebSocketSession session, JsonObject jsonMessage, String token) throws IOException {
        String presenterIdx = Integer.toString(jsonMessage.getAsJsonPrimitive("presenterIdx").getAsInt());
        String viewerIdx = jwtUtils.decodeTokenToId(token, session);
        StreamingInfo streamingInfo = streamingInfoRepository.getStreamingInfo(presenterIdx);

        sessionRepository.addSession(session.getId(),
                SessionInfo.builder().
                userIdx(viewerIdx).
                isPresenter(false).
                token(token).
                presenterIdx(presenterIdx).
                build());

        try {
            if (streamingInfo == null) {
                sendMessageService.sendMsgError(session, responseMessage.PRESENT_NO_ROOM,
                        ErrorResponseEnum.UserException.getMessage());
                throw new NotStartedException(session, responseMessage.VIEWER_NOT_STARTED,
                        ErrorResponseEnum.UserException.getMessage());
            } else if (viewerConnectionRepository.isContainsKey(session.getId())) {
                sendMessageService.sendMsgError(session, responseMessage.VIEWER_ALREADY_VIEWING,
                        ErrorResponseEnum.UserException.getMessage());
                throw new AlreadyJoinedException(session, responseMessage.VIEWER_ALREADY_VIEWING,
                        ErrorResponseEnum.UserException.getMessage());
            } else {
                MediaPipeline roomPipeLine = getMediaServerUtil.findPipeline(streamingInfo.getPipelineId());
                WebRtcEndpoint presenterEndpoint = getMediaServerUtil.findEndpoint(streamingInfo.getWebRtcEndPointId());

                WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.
                        Builder(roomPipeLine).useDataChannels().build();
                nextWebRtc.setOutputBitrate(2147483647);
                nextWebRtc.setMinVideoSendBandwidth(500);
                nextWebRtc.setMaxVideoSendBandwidth(500);
                nextWebRtc.setTurnUrl("testuser:root@117.17.196.61:3478", new Continuation<Void>() {
                    @Override
                    public void onSuccess(Void result) throws Exception {
                        log.debug("Set Turn Success");
                    }

                    @Override
                    public void onError(Throwable cause) throws Exception {

                    }
                });

                iceCandidateUtil.addIceCandidateFoundListener(session, nextWebRtc);

                presenterEndpoint.connect(nextWebRtc);
                String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
                String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

                Mono<?> joinRoomResponse = httpRequestUtils.setHttpRequest(
                        token, RequestUriEnum.JoinRoom.getMessage(),
                        JoinRoomRequestDto.builder().presenterIdx(presenterIdx).build());

                joinRoomResponse.subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object returnObject) {
                        log.info(returnObject.toString());
                        if(returnObject instanceof Message) {
                            Message message = (Message)returnObject;

                            joinedViewerRepository.addViewer(presenterIdx, session.getId(), session);
                            viewerConnectionRepository.addViewerConnection(session.getId(),StreamingInfo.
                                    builder().
                                    webRtcEndPointId(nextWebRtc.getId()).
                                    pipelineId(roomPipeLine.getId()).build());

                            sendMessageService.sendMsgToViewer(session, sdpAnswer, message);
                            nextWebRtc.gatherCandidates();
                        } else {
                            ErrorMessage errorMessage = (ErrorMessage)returnObject;
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

                return;
            }
        } catch (NullPointerException nullPointerException) {
            log.error("{} {}", this.getClass(), nullPointerException);

            sendMessageService.sendMsgError(session, responseMessage.NULL_POINT_EXCEPTION_MESSAGE);
            session.close();
        } catch (Exception unKnownException) {
            log.error("{} {}", this.getClass(), unKnownException);

            sendMessageService.sendMsgError(session, responseMessage.UNPREDICTED_EXCEPTION);
            session.close();
        }
    }

    public void stopViewer(WebSocketSession session, JsonObject jsonMessage) {
        try {
            WebRtcEndpoint viewerEndpoint = getMediaServerUtil.findEndpoint(viewerConnectionRepository
                    .getStreamingInfo(session.getId()).getWebRtcEndPointId());

            String token = jsonMessage.getAsJsonPrimitive("token").getAsString();
            String presenterIdx = Long.toString(jsonMessage.getAsJsonPrimitive("presenterIdx").getAsLong());
            String viewerIdx = jwtUtils.decodeTokenToId(token, session);

            Mono<?> leaveRoomResponse = httpRequestUtils.setHttpRequest(
                    token, RequestUriEnum.LeaveRoom.getMessage(),
                    LeaveRoomRequestDto.builder().presenterIdx(presenterIdx).build());

            Object object = leaveRoomResponse.block();

            joinedViewerRepository.deleteViewer(presenterIdx, viewerIdx);
            viewerConnectionRepository.deleteViewerConnection(session.getId());
            sessionRepository.deleteSession(session.getId());
            sendMessageService.sendStopCommunication(session);
            viewerEndpoint.release();


            if(object == null) {
                sendMessageService.sendStopCommunication(session);
            } else {
                ErrorMessage errorMessage = (ErrorMessage)object;
                sendMessageService.sendMsgError(session,
                        errorMessage.getMessage(),
                        ErrorResponseEnum.ServerException.getMessage());
            }


            return;
        } catch (NullPointerException nullPointerException) {
            log.error("{} {}", this.getClass(), nullPointerException);

            sendMessageService.sendMsgError(session, responseMessage.NULL_POINT_EXCEPTION_MESSAGE,
                    ErrorResponseEnum.UserException.getMessage());
        } catch (Exception unKnownException) {
            log.error("{} {}", this.getClass(), unKnownException);

            sendMessageService.sendMsgError(session, responseMessage.UNPREDICTED_EXCEPTION,
                    ErrorResponseEnum.ServerException.getMessage());
        }
    }
}
