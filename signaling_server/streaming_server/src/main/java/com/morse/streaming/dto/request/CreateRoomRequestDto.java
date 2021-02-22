package com.morse.streaming.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateRoomRequestDto {
    private String title;
    private String contents;

    @Builder
    CreateRoomRequestDto(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }
}