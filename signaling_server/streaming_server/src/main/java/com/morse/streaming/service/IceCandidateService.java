package com.morse.streaming.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.morse.streaming.enums.ErrorResponseEnum;
import com.morse.streaming.exception.IceCandidateException;
import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.repository.StreamingInfoRepository;
import com.morse.streaming.repository.ViewerConnectionRepository;
import com.morse.streaming.util.GetMediaServerUtil;
import com.morse.streaming.util.JWTUtils;
import com.morse.streaming.util.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class IceCandidateService {
    private final JWTUtils jwtUtils;
    private final StreamingInfoRepository streamingInfoRepository;
    private final ViewerConnectionRepository viewerConnectionRepository;
    private final GetMediaServerUtil getMediaServerUtil;
    private final SendMessageService sendMessageService;
    private final ResponseMessage responseMessage;

    public void iceCandidate(WebSocketSession session, JsonObject jsonMessage, String token) throws IOException {
        try {
            WebRtcEndpoint webRtcEndpoint = null;
            JsonParser jsonParser = new JsonParser();
            JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
            boolean isPresenter = jsonMessage.getAsJsonPrimitive("isPresenter").getAsBoolean();
            StreamingInfo streamingInfo = null;

            jsonMessage.toString();
            if (isPresenter) {
                String userIdx = jwtUtils.decodeTokenToId(token, session);
                streamingInfo = streamingInfoRepository.getStreamingInfo(userIdx);
            } else {
                streamingInfo = viewerConnectionRepository.getStreamingInfo(session.getId());
            }

            if (streamingInfo != null) {
                webRtcEndpoint = getMediaServerUtil.findEndpoint(streamingInfo.getWebRtcEndPointId());
                if (webRtcEndpoint != null) {
                    IceCandidate cand =
                            new IceCandidate(candidate.get("candidate").getAsString(),
                                    candidate.get("sdpMid").getAsString(),
                                    candidate.get("sdpMLineIndex").getAsInt());
                    webRtcEndpoint.addIceCandidate(cand);
                }
            }
        } catch (Exception e) {
            throw new IceCandidateException();
        }
    }
}
