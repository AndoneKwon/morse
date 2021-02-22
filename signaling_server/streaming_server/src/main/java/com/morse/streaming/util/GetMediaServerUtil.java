package com.morse.streaming.util;

import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetMediaServerUtil {
    private final KurentoClient kurento;

    /* Find Media Pipeline By String*/
    public MediaPipeline findPipeline(String pipeline) {
        MediaPipeline findPipeline = kurento.getById(pipeline, MediaPipeline.class);
        if (findPipeline != null) {
            return findPipeline;
        }
        return null;
    }

    /* Find Media Endpoint By String*/
    public WebRtcEndpoint findEndpoint(String endpoint) {
        WebRtcEndpoint findEndpoint = kurento.getById(endpoint, WebRtcEndpoint.class);
        if (findEndpoint != null) {
            return findEndpoint;
        }
        return null;
    }

    /* Find Media Endpoint By String*/
    public RecorderEndpoint findEndRecorderEndpoint(String endpoint) {
        RecorderEndpoint recorderEndpoint = kurento.getById(endpoint, RecorderEndpoint.class);
        if (recorderEndpoint != null) {
            return recorderEndpoint;
        }
        return null;
    }
}
