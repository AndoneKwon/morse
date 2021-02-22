package com.morse.streaming.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVideoThumnailRequestDTO {
	private String filePath;
	private String encodingType;
	private int start;
	private int time;
}
