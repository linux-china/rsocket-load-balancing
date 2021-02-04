package com.vinsguru.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@MessageMapping("com.example.calculator.ExchangeCalculatorService")
public class ExchangeCalculatorController implements ExchangeCalculatorService {
    @Override
    @MessageMapping("exchange")
    public double exchange(ExchangeRequest request) {
        if (request.getSource().equals("USD") && request.getTarget().equals("CNY")) {
            return request.getAmount() * 6.4;
        }
        return 0.0;
    }
}
