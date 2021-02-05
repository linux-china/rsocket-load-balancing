package com.example.calculator;

import reactor.core.publisher.Mono;

public interface MathCalculatorService {
    String RSOCKET_SERVICE_NAME = "com.example.calculator.MathCalculatorService";

    Mono<Integer> square(Integer input);
}
