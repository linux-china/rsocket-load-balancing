package com.example.calculator.impl;

import com.example.calculator.ExchangeCalculatorService;
import com.example.calculator.ExchangeRequest;
import com.example.calculator.annotations.RSocketHandler;
import com.example.calculator.annotations.RSocketService;

@RSocketService("com.example.calculator.ExchangeCalculatorService")
public class ExchangeCalculatorController implements ExchangeCalculatorService {
    @Override
    @RSocketHandler("exchange")
    public double exchange(ExchangeRequest request) {
        if (request.getSource().equals("USD") && request.getTarget().equals("CNY")) {
            return request.getAmount() * 6.4;
        }
        return 0.0;
    }
}
