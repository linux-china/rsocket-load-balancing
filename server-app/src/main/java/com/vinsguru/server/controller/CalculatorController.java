package com.vinsguru.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@MessageMapping("com.example.CalculatorService")
public class CalculatorController implements CalculatorService {

    @MessageMapping("square")
    public Mono<Integer> square(Integer input) {
        System.out.println("received: " + input);
        return Mono.just(input * input);
    }
}
