package com.example.rsocket.client.proxy;

import reactor.core.publisher.Mono;

public interface MathCalculatorService {
    Mono<Integer> square(Integer input);
}
