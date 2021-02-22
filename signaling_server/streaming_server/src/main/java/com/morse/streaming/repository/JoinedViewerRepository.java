package com.morse.streaming.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;


import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@RequiredArgsConstructor
public class JoinedViewerRepository {
    ConcurrentMap<String, HashMap<String, WebSocketSession>> roomMap = new ConcurrentHashMap<>();

    public void makeNewRoom (String presenterIdx) {
        this.roomMap.put(presenterIdx, new HashMap<>());
    }

    public void addViewer(String presenterIdx, String sessionId, WebSocketSession webSocketSession) {
        this.roomMap.get(presenterIdx).put(sessionId,webSocketSession);
    }

    public Collection<WebSocketSession> getViewerCollection(String presenterIdx) {
        return this.roomMap.get(presenterIdx).values();
    }

    public void deleteViewer(String presenterIdx, String viewerIdx) {
        this.roomMap.get(presenterIdx).remove(viewerIdx);
    }
}
