package com.morse.streaming.contorller;

import com.morse.streaming.dto.message.Message;
import com.morse.streaming.dto.request.StopCommunicationRequestDto;
import com.morse.streaming.service.RollbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ObservingController {
    private final RollbackService rollbackService;

    @PostMapping("/release")
    public ResponseEntity<Message> releaseEndpoint(@RequestBody StopCommunicationRequestDto stopCommunicationRequestDto) throws IOException {
        rollbackService.unexpectedStop(stopCommunicationRequestDto.getPresenterIdx());

        return ResponseEntity.ok().build();
    }
}
