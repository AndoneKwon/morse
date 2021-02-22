package com.morse.streaming.service;

import com.morse.streaming.model.StreamingInfo;
import com.morse.streaming.util.GetMediaServerUtil;
import org.kurento.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.morse.streaming.dto.request.CreateImageThumnailRequestDTO;
import com.morse.streaming.dto.request.CreateVideoThumnailRequestDTO;
import com.morse.streaming.util.SetHttpRequestUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordService {
    private static final int VEDIO_THUMNAIL_START_DEFAULT = 5;
    private static final int VEDIO_THUMNAIL_TIME_DEFAULT = 20;
    private static final int IMAGE_THUMNAIL_START_DEFAULT = 5;

    private static final String FILE_PATH = "file:///home/sshuser/data2/";
    private static final String VIDEO_ENCODING_FORMAT = ".webm";

    private final SetHttpRequestUtils httpRequestUtils;
    private final String fileServer = "http://goto.downsups.kro.kr:8442/file";
    private final GetMediaServerUtil getMediaServerUtil;

    public StreamingInfo record(MediaPipeline pipeline, WebRtcEndpoint presenterEndpoint, String recordLocation, int roomIdx) {
        RecorderEndpoint recorderEndpoint = new RecorderEndpoint.
                Builder(pipeline, getUri(recordLocation, roomIdx)).
                build();

        addMediaFlowInStateChangeListener(recorderEndpoint);

        addElementDisconnectedListener(recordLocation, roomIdx, recorderEndpoint);

        presenterEndpoint.connect(recorderEndpoint);

        recorderEndpoint.record(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("record start");
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.info(cause.toString());
            }
        });

        return StreamingInfo.builder().
                pipelineId(pipeline.getId()).
                webRtcEndPointId(presenterEndpoint.getId())
                .recorderEndPoint(recorderEndpoint.getId())
                .recordLocation(recordLocation)
                .roomIdx(roomIdx)
                .build();
    }

    private void addElementDisconnectedListener(String recordLocation, int roomIdx, RecorderEndpoint recorderEndpoint) {
        recorderEndpoint.addStoppedListener(new EventListener<StoppedEvent>() {
            @Override
            public void onEvent(StoppedEvent event) {

            }
        });
    }

    private void addMediaFlowInStateChangeListener(RecorderEndpoint recorderEndpoint) {
        recorderEndpoint.addMediaFlowInStateChangeListener(new EventListener<MediaFlowInStateChangeEvent>() {
            @Override
            public void onEvent(MediaFlowInStateChangeEvent event) {
                if (event.getState().equals(MediaFlowState.FLOWING)) {
                    log.info("media flowing");

                }
                if (event.getState().equals(MediaFlowState.NOT_FLOWING)) {
                    log.info("media not flowing");
                    recorderEndpoint.stopAndWait();
                }
            }
        });
    }

    private String getUri(String recordLocation, long roomIdx) {
        return new StringBuilder().
                append(FILE_PATH).
                append(recordLocation).
                append(roomIdx).
                append(VIDEO_ENCODING_FORMAT).toString();
    }

    private String getFilePath(String recordLocation, long roomIdx) {
        return new StringBuilder().
                append(FILE_PATH).
                append(recordLocation).
                append(roomIdx).toString();
    }

    private void doRelease(StreamingInfo streamingInfo) {
        WebRtcEndpoint endpoint = getMediaServerUtil.findEndpoint(streamingInfo.getWebRtcEndPointId());
        MediaPipeline pipeline = getMediaServerUtil.findPipeline(streamingInfo.getPipelineId());
        RecorderEndpoint recorderEndpoint = getMediaServerUtil.findEndRecorderEndpoint(streamingInfo.getWebRtcEndPointId());

        endpoint.release();
        pipeline.release();
        recorderEndpoint.release();
    }

    public void releaseAll(StreamingInfo streamingInfo) {
        log.info("Element Disconnected");
        WebRtcEndpoint endpoint = getMediaServerUtil.findEndpoint(streamingInfo.getWebRtcEndPointId());
        MediaPipeline pipeline = getMediaServerUtil.findPipeline(streamingInfo.getPipelineId());
        RecorderEndpoint recorderEndpoint = getMediaServerUtil.findEndRecorderEndpoint(streamingInfo.getRecorderEndPoint());

        endpoint.release();
        pipeline.release();
        recorderEndpoint.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("record release");
                Mono<?> videoThumnailResponse = httpRequestUtils.setHttpThumbRequest(
                        fileServer,
                        "/thumnail/video",
                        CreateVideoThumnailRequestDTO.builder().
                                filePath(getFilePath(streamingInfo.getRecordLocation(), streamingInfo.getRoomIdx())).
                                encodingType(VIDEO_ENCODING_FORMAT).
                                start(VEDIO_THUMNAIL_START_DEFAULT).
                                time(VEDIO_THUMNAIL_TIME_DEFAULT).
                                build());
                videoThumnailResponse.subscribe();

                Mono<?> imageThumnailResponse = httpRequestUtils.setHttpThumbRequest(
                        fileServer,
                        "/thumnail/image",
                        CreateImageThumnailRequestDTO.builder().
                                filePath(getFilePath(streamingInfo.getRecordLocation(), streamingInfo.getRoomIdx())).
                                encodingType(VIDEO_ENCODING_FORMAT).
                                start(IMAGE_THUMNAIL_START_DEFAULT).
                                build());
                imageThumnailResponse.subscribe();
            }

            @Override
            public void onError(Throwable cause) throws Exception {

            }
        });
    }
}
