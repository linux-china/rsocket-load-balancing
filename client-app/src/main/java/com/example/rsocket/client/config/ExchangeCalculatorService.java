package com.example.rsocket.client.config;

public interface ExchangeCalculatorService {
    String RSOCKET_SERVICE_NAME = "com.example.calculator.ExchangeCalculatorService";

    double exchange(ExchangeRequest request);

    default double rmbToDollar(double amount) {

        return exchange(new ExchangeRequest(amount, "CNY", "USD"));
    }

}
