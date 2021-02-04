package com.example.rsocket.client.config;

import org.mvnsearch.rsocket.loadbalance.RSocketServiceRegistry;
import org.mvnsearch.rsocket.loadbalance.proxy.RSocketRemoteServiceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

@Configuration
public class RSocketConfig {

    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                .build();
    }

    @Bean
    public RSocketRequester mathCalculatorRequester(RSocketRequester.Builder builder,
                                                    RSocketServiceRegistry rsocketServiceRegistry) {
        return rsocketServiceRegistry.buildLoadBalanceRSocket(MathCalculatorService.RSOCKET_SERVICE_NAME, builder);
    }

    @Bean
    public RSocketRequester exchangeCalculatorRequester(RSocketRequester.Builder builder,
                                                        RSocketServiceRegistry rsocketServiceRegistry) {
        return rsocketServiceRegistry.buildLoadBalanceRSocket("com.example.calculator.ExchangeCalculatorService", builder);
    }

    @Bean
    public MathCalculatorService mathCalculatorService(RSocketRequester mathCalculatorRequester) {
        RSocketRemoteServiceBuilder<MathCalculatorService> builder = new RSocketRemoteServiceBuilder<>();
        return builder.serviceName(MathCalculatorService.RSOCKET_SERVICE_NAME)
                .serviceInterface(MathCalculatorService.class)
                .rsocketRequester(mathCalculatorRequester)
                .build();
    }

}
