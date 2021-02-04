package com.example.calculator.controller;

import com.example.calculator.MathCalculatorService;
import com.example.calculator.annotations.RSocketHandler;
import com.example.calculator.annotations.RSocketService;
import reactor.core.publisher.Mono;

@RSocketService("com.example.calculator.MathCalculatorService")
public class MathCalculatorController implements MathCalculatorService {

    @RSocketHandler("square")
    public Mono<Integer> square(Integer input) {
        System.out.println("received: " + input);
        return Mono.just(input * input);
    }
}
