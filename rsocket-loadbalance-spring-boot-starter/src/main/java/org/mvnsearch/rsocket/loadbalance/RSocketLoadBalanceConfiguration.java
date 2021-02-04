package org.mvnsearch.rsocket.loadbalance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RSocketLoadBalanceConfiguration {

    @Bean
    public RSocketServiceDiscoveryRegistry rsocketServiceDiscoveryRegistry() {
        return new RSocketServiceDiscoveryRegistry();
    }

}
