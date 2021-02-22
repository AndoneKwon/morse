package com.morse.streaming.repository;

import com.morse.streaming.model.SessionInfo;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/* Presenter 강제 종료를 위한 Session 정보 */
@Repository
public class PresenterSessionRepository {
    final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void addSession(String presenterIdx, WebSocketSession webSocketSession) {

        this.sessionMap.put(presenterIdx, webSocketSession);
    }

    public void deleteSession(String presenterIdx) {
        this.sessionMap.remove(presenterIdx);
    }

    public WebSocketSession getSessionInfo(String presenterIdx) {
        return this.sessionMap.get(presenterIdx);
    }
}
