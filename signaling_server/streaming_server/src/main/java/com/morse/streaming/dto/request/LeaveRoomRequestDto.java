package com.morse.streaming.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LeaveRoomRequestDto {
    private String presenterIdx;

    @Builder
    public LeaveRoomRequestDto(String presenterIdx) {
        this.presenterIdx = presenterIdx;
    }
}
