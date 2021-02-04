package com.vinsguru.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;

@RestController
public class PortalController {
    @Autowired
    private RSocketRequester calculatorRequester;

    @GetMapping("/")
    public Mono<String> index() {
        int number = new Random().nextInt(20);
        return calculatorRequester.route("com.example.CalculatorService.square")
                .data(number)
                .retrieveMono(Integer.class)
                .map(result -> number + "*" + number + "=" + result);
    }
}
