package com.morse.streaming.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinRoomRequestDto {
    private String presenterIdx;

    @Builder
    public JoinRoomRequestDto(String presenterIdx) {
        this.presenterIdx = presenterIdx;
    }
}
