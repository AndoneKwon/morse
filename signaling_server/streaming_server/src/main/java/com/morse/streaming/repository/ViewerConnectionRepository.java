package com.morse.streaming.repository;

import com.morse.streaming.model.StreamingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Repository
public class ViewerConnectionRepository {
    ConcurrentHashMap<String, StreamingInfo> viewerConnectionMap = new ConcurrentHashMap<>();

    public void addViewerConnection(String sessionId, StreamingInfo streamingInfo) {
        this.viewerConnectionMap.put(sessionId, streamingInfo);
    }

    public void deleteViewerConnection(String sessionId) {
        this.viewerConnectionMap.remove(sessionId);
    }

    public StreamingInfo getStreamingInfo(String sessionId) {
        return this.viewerConnectionMap.get(sessionId);
    }

    public boolean isContainsKey(String sessionId) {
        return this.viewerConnectionMap.containsKey(sessionId);
    }
}
