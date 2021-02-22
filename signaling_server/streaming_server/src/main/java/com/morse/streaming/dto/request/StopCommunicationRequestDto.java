package com.morse.streaming.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StopCommunicationRequestDto {
    String presenterIdx;

    @Builder
    StopCommunicationRequestDto(String presenterIdx) {
        this.presenterIdx = presenterIdx;
    }
}
