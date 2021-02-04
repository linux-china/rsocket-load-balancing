package com.example.calculator;

import reactor.core.publisher.Mono;

public interface ExchangeCalculatorService {

    Mono<Double> exchange(ExchangeRequest request);
}
