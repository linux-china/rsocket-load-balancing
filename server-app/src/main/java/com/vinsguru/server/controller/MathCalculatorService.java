package com.vinsguru.server.controller;

import reactor.core.publisher.Mono;

public interface MathCalculatorService {
    Mono<Integer> square(Integer input);
}
