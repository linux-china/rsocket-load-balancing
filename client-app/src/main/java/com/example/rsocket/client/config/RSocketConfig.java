package com.example.rsocket.client.config;

import com.example.rsocket.client.proxy.MathCalculatorService;
import com.example.rsocket.client.proxy.RSocketRemoteServiceBuilder;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
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
        return builder.transports(rsocketServiceRegistry.getServers("com.example.calculator.MathCalculatorService"),
                new RoundRobinLoadbalanceStrategy());
    }

    @Bean
    public RSocketRequester exchangeCalculatorRequester(RSocketRequester.Builder builder,
                                                        RSocketServiceRegistry rsocketServiceRegistry) {
        return builder.transports(rsocketServiceRegistry.getServers("com.example.calculator.ExchangeCalculatorService"),
                new RoundRobinLoadbalanceStrategy());
    }

    @Bean
    public MathCalculatorService mathCalculatorService(RSocketRequester mathCalculatorRequester) {
        RSocketRemoteServiceBuilder<MathCalculatorService> builder = new RSocketRemoteServiceBuilder<>();
        return builder.serviceName("com.example.calculator.MathCalculatorService").
                serviceInterface(MathCalculatorService.class)
                .rsocketRequester(mathCalculatorRequester)
                .build();
    }

}
