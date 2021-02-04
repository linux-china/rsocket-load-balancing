package com.vinsguru.client.config.impl;

import com.vinsguru.client.config.RSocketServerInstance;
import com.vinsguru.client.config.RSocketServiceRegistry;
import io.rsocket.loadbalance.LoadbalanceTarget;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConfigurationProperties(prefix = "rsocket.square-service")
public class DummyServiceRegistry implements RSocketServiceRegistry {

    private List<RSocketServerInstance> servers;
    private Sinks.Many<List<RSocketServerInstance>> serversSink = Sinks.many().replay().latest();

    public void setServers(List<RSocketServerInstance> servers) {
        this.servers = servers;
        this.serversSink.tryEmitNext(servers);
    }

    public Flux<List<LoadbalanceTarget>> getServers() {
        return serversSink.asFlux()
                .map(this::toLoadBalanceTarget);
    }

    private List<LoadbalanceTarget> toLoadBalanceTarget(List<RSocketServerInstance> rSocketServers) {
        return rSocketServers.stream()
                .map(server -> LoadbalanceTarget.from(server.getHost() + server.getPort(), server.constructClientTransport()))
                .collect(Collectors.toList());
    }

}
