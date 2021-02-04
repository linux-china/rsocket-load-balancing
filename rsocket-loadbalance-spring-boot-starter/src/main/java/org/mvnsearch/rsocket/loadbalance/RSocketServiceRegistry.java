package org.mvnsearch.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface RSocketServiceRegistry {
    Flux<List<LoadbalanceTarget>> getServers(String serviceName);

    RSocketRequester buildLoadBalanceRSocket(String serviceName, RSocketRequester.Builder builder);

    Map<String, List<RSocketServerInstance>> getSnapshots();

    Date getLastRefreshTimestamp();
}
