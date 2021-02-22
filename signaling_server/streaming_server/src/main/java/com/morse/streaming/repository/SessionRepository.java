package com.morse.streaming.repository;

import com.morse.streaming.model.SessionInfo;
import com.morse.streaming.model.StreamingInfo;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

/* Session에 대한 연결관리를 해주기 위한 Repository */
@Repository
public class SessionRepository {
    final ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    public void addSession(String sessionId, SessionInfo sessionInfo) {
        this.sessionMap.put(sessionId, sessionInfo);
    }

    public void deleteSession(String sessionId) {
        this.sessionMap.remove(sessionId);
    }

    public SessionInfo getSessionInfo(String sessionId) {
        return this.sessionMap.get(sessionId);
    }
}
