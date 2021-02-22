package com.morse.streaming.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.web.socket.WebSocketSession;


@Getter
@Setter
@NoArgsConstructor
public class StreamingInfo {
    private String pipelineId;
    private String webRtcEndPointId;
    private String recorderEndPoint;
    private String recordLocation;
    private long roomIdx;

    @Builder
    public StreamingInfo(String pipelineId, String webRtcEndPointId, String recorderEndPoint,
                         String recordLocation, long roomIdx) {

        this.pipelineId = pipelineId;
        this.webRtcEndPointId = webRtcEndPointId;
        this.recorderEndPoint = recorderEndPoint;
        this.recordLocation = recordLocation;
        this.roomIdx = roomIdx;
    }
}