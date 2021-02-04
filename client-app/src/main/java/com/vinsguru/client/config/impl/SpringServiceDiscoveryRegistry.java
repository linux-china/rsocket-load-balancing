package com.vinsguru.client.config.impl;

import com.vinsguru.client.config.RSocketServerInstance;
import com.vinsguru.client.config.RSocketServiceRegistry;
import io.rsocket.loadbalance.LoadbalanceTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SpringServiceDiscoveryRegistry implements RSocketServiceRegistry {
    private Map<String, Sinks.Many<List<RSocketServerInstance>>> service2Servers = new ConcurrentHashMap<>();

    @Autowired
    private ReactiveDiscoveryClient discoveryClient;

    public void setServers(String serviceName, List<RSocketServerInstance> servers) {
        serviceName = convertToDiscoveryServiceName(serviceName);
        if (service2Servers.containsKey(serviceName)) {
            this.service2Servers.get(serviceName).tryEmitNext(servers);
        }
    }

    public Flux<List<LoadbalanceTarget>> getServers(String serviceName) {
        final String discoveryServiceName = convertToDiscoveryServiceName(serviceName);
        if (!service2Servers.containsKey(discoveryServiceName)) {
            service2Servers.put(discoveryServiceName, Sinks.many().replay().latest());
            return Flux.from(discoveryClient.getInstances(discoveryServiceName)
                    .map(serviceInstance -> {
                        String host = serviceInstance.getHost();
                        String rsocketPort = serviceInstance.getMetadata().getOrDefault("rsocketPort", "6565");
                        return new RSocketServerInstance(host, Integer.parseInt(rsocketPort));
                    })
                    .collectList()
                    .doOnNext(rSocketServerInstances -> service2Servers.get(discoveryServiceName).tryEmitNext(rSocketServerInstances)))
                    .thenMany(service2Servers.get(discoveryServiceName).asFlux().map(this::toLoadBalanceTarget));
        }
        return service2Servers.get(discoveryServiceName)
                .asFlux()
                .map(this::toLoadBalanceTarget);
    }

    private List<LoadbalanceTarget> toLoadBalanceTarget(List<RSocketServerInstance> rSocketServers) {
        return rSocketServers.stream()
                .map(server -> LoadbalanceTarget.from(server.getHost() + server.getPort(), server.constructClientTransport()))
                .collect(Collectors.toList());
    }

    private String convertToDiscoveryServiceName(String serviceName) {
        return serviceName.replaceAll("\\.", "-");
    }

}
