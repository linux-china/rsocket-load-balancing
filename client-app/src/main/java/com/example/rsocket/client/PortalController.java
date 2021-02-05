package com.example.rsocket.client;

import com.example.calculator.ExchangeCalculatorService;
import com.example.calculator.MathCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PortalController {
    @Autowired
    private RSocketRequester mathCalculatorRequester;
    @Autowired
    private MathCalculatorService mathCalculatorService;
    @Autowired
    private ExchangeCalculatorService exchangeCalculatorService;

    @GetMapping("/square/{number}")
    public Mono<String> square(@PathVariable("number") int number) {
        return mathCalculatorService.square(number)
                .map(result -> number + "*" + number + "=" + result);
    }

    @GetMapping("/square2/{number}")
    public Mono<String> square2(@PathVariable("number") int number) {
        return mathCalculatorRequester.route("com.example.calculator.MathCalculatorService.square")
                .data(number)
                .retrieveMono(Integer.class)
                .map(result -> number + "*" + number + "=" + result);
    }

    @GetMapping("/exchange/{number}")
    public Mono<String> exchangeUSD2CNY(@PathVariable("number") int number) {
        return exchangeCalculatorService.dollarToRMB(number)
                .map(result -> number + "$ = " + result + "RMB");
    }
}
