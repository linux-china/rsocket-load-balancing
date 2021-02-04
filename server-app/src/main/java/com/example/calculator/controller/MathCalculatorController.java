package com.example.calculator.controller;

import com.example.calculator.MathCalculatorService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@MessageMapping("com.example.calculator.MathCalculatorService")
public class MathCalculatorController implements MathCalculatorService {

    @MessageMapping("square")
    public Mono<Integer> square(Integer input) {
        System.out.println("received: " + input);
        return Mono.just(input * input);
    }
}
