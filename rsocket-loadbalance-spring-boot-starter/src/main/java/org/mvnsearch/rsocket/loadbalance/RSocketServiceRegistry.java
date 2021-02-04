package org.mvnsearch.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RSocketServiceRegistry {
    Flux<List<LoadbalanceTarget>> getServers(String serviceName);

    RSocketRequester buildLoadBalanceRSocket(String serviceName, RSocketRequester.Builder builder);
}
