package org.mvnsearch.rsocket.loadbalance;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RSocketLoadBalanceConfiguration {

    @Bean
    public RSocketServiceDiscoveryRegistry rsocketServiceDiscoveryRegistry(ReactiveDiscoveryClient discoveryClient) {
        return new RSocketServiceDiscoveryRegistry(discoveryClient);
    }

    @Bean
    public RSocketLoadBalanceEndpoint rsocketLoadBalanceEndpoint(RSocketServiceRegistry rsocketServiceRegistry) {
        return new RSocketLoadBalanceEndpoint(rsocketServiceRegistry);
    }
}
