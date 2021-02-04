package com.vinsguru.server.controller;

import reactor.core.publisher.Mono;

public interface CalculatorService {
    Mono<Integer> square(Integer input);
}
