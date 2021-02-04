package com.vinsguru.client.config;

import io.rsocket.loadbalance.LoadbalanceTarget;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RSocketServiceRegistry {
    Flux<List<LoadbalanceTarget>> getServers(String serviceName);
}
