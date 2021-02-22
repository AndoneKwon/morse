package com.morse.streaming.model;

import lombok.Builder;
import lombok.NoArgsConstructor;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
@NoArgsConstructor
public class JoinedViewer {
    private Long viewerIdx;
    private String nickname;
    private String webRtcEndpointId;

    @Builder
    public JoinedViewer(Long viewerIdx, String nickname, String webRtcEndpointId) {
        this.viewerIdx = viewerIdx;
        this.nickname = nickname;
        this.webRtcEndpointId = webRtcEndpointId;
    }
}
