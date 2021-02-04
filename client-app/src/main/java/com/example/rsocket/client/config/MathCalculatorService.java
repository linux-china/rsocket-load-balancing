package com.example.rsocket.client.config;

import reactor.core.publisher.Mono;

public interface MathCalculatorService {
    String RSOCKET_SERVICE_NAME = "com.example.calculator.MathCalculatorService";

    Mono<Integer> square(Integer input);
}
