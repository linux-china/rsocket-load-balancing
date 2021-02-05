package com.example.calculator;

import reactor.core.publisher.Mono;

public interface ExchangeCalculatorService {
    String RSOCKET_SERVICE_NAME = "com.example.calculator.ExchangeCalculatorService";

    Mono<Double> exchange(ExchangeRequest request);

    default Mono<Double> dollarToRMB(double amount) {
        return exchange(new ExchangeRequest(amount, "USD", "CNY"));
    }

}
