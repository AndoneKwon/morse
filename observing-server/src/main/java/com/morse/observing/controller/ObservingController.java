package com.morse.observing.controller;

import com.morse.observing.service.ObservingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
public class ObservingController {
    private final ObservingService observingService;

    @GetMapping("/")
    public void viewingStatusController (){
        observingService.startObserving();
    }
}
