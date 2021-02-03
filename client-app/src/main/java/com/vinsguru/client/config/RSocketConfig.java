package com.vinsguru.client.config;

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
    public RSocketRequester rSocketClient(RSocketRequester.Builder builder,
                                          RSocketServiceRegistry rsocketServiceRegistry) {
        return builder.transports(rsocketServiceRegistry.getServers(), new RoundRobinLoadbalanceStrategy());
    }

}
