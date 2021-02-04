package com.example.calculator;

import reactor.core.publisher.Mono;

public interface MathCalculatorService {
    Mono<Integer> square(Integer input);
}
