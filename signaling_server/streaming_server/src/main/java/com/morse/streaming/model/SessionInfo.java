package com.morse.streaming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionInfo {
    boolean isPresenter;
    String userIdx;
    String token;
    String presenterIdx;

    @Builder
    SessionInfo(boolean isPresenter, String userIdx, String token, String presenterIdx) {
        this.isPresenter = isPresenter;
        this.userIdx = userIdx;
        this.token = token;
        this.presenterIdx = presenterIdx;
    }
}
