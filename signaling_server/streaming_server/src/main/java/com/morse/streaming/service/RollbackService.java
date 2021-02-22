package com.morse.streaming.service;

import com.morse.streaming.dto.message.ErrorMessage;
import com.morse.streaming.dto.message.Message;
import com.morse.streaming.dto.request.LeaveRoomRequestDto;
import com.morse.streaming.enums.RequestUriEnum;
import com.morse.streaming.exception.RoomServerErrorException;
import com.morse.streaming.model.SessionInfo;
import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.repository.JoinedViewerRepository;
import com.morse.streaming.repository.PresenterSessionRepository;
import com.morse.streaming.repository.StreamingInfoRepository;
import com.morse.streaming.repository.ViewerConnectionRepository;
import com.morse.streaming.util.GetMediaServerUtil;
import com.morse.streaming.util.SetHttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class RollbackService {
    private final StreamingInfoRepository streamingInfoRepository;
    private final GetMediaServerUtil getMediaServerUtil;
    private final SetHttpRequestUtils httpRequestUtils;
    private final WebClient webClient = WebClient.create("http://downsups.onstove.com:8005/room");
    private final SendMessageService sendMessageService;
    private final ViewerConnectionRepository viewerConnectionRepository;
    private final JoinedViewerRepository joinedViewerRepository;
    private final RecordService recordService;
    private final PresenterSessionRepository presenterSessionRepository;

    /* Observing Server */
    public void unexpectedStop(String presenterIdx) throws IOException {
        log.info("Unexpected Presenter Stop");
        Collection<WebSocketSession> viewerSessions = joinedViewerRepository.getViewerCollection(presenterIdx);
        for(WebSocketSession viewerSession : viewerSessions) {
            sendMessageService.sendStopCommunication(viewerSession);
        }
        StreamingInfo streamingInfo = streamingInfoRepository.getStreamingInfo(presenterIdx);

        recordService.releaseAll(streamingInfo);

        streamingInfoRepository.deleteStreamingInfo(presenterIdx);
        presenterSessionRepository.getSessionInfo(presenterIdx).close();
        presenterSessionRepository.deleteSession(presenterIdx);

        Mono<Object> responseSpec = webClient.get().uri(uriBuilder -> uriBuilder
                .path("/live/close/signaling")
                .queryParam("presenterIdx",Integer.parseInt(presenterIdx))
                .build(Integer.parseInt(presenterIdx)))
                .retrieve().bodyToMono(Object.class);

        responseSpec.block();
        log.info("Close finish");

        log.info("Endpoint Release");
    }

    public void stopPresenter(SessionInfo sessionInfo){
        String presenterIdx = sessionInfo.getUserIdx();
        StreamingInfo streamingInfo = streamingInfoRepository.getStreamingInfo(presenterIdx);
        MediaPipeline mediaPipeline = getMediaServerUtil.findPipeline(streamingInfo.getPipelineId());
        WebRtcEndpoint presenterEndpoint = getMediaServerUtil.findEndpoint(streamingInfo.getWebRtcEndPointId());

        Mono<?> stopRoomResponse = httpRequestUtils.setHttpRequest(
                sessionInfo.getToken(), RequestUriEnum.CloseRoom.getMessage(),
                LeaveRoomRequestDto.builder().presenterIdx(sessionInfo.getUserIdx()).build());


        Object responseFromRoomServer = stopRoomResponse.block();

        recordService.releaseAll(streamingInfo);
        streamingInfoRepository.deleteStreamingInfo(presenterIdx);
        presenterSessionRepository.deleteSession(presenterIdx);

        Collection<WebSocketSession> viewerSessions =joinedViewerRepository.getViewerCollection(presenterIdx);
        for(WebSocketSession viewerSession : viewerSessions) {
            sendMessageService.sendStopCommunication(viewerSession);
        }
        if (responseFromRoomServer == null) {
        } else {
            ErrorMessage errorMessage = (ErrorMessage)responseFromRoomServer;
            throw new RoomServerErrorException(errorMessage.getMessage());
        }
    }

    public void stopViewer(String sessionId, SessionInfo sessionInfo) {
        WebRtcEndpoint viewerEndpoint = getMediaServerUtil.
                findEndpoint(viewerConnectionRepository.getStreamingInfo(sessionId).getWebRtcEndPointId());

        Mono<?> leaveRoomResponse = httpRequestUtils.setHttpRequest(
                sessionInfo.getToken(), RequestUriEnum.LeaveRoom.getMessage(),
                LeaveRoomRequestDto.builder().presenterIdx(sessionInfo.getPresenterIdx()).build());

        viewerConnectionRepository.deleteViewerConnection(sessionInfo.getPresenterIdx());
        viewerEndpoint.release();

        Object responseFromRoomServer = leaveRoomResponse.block();

        return;
    }
}
