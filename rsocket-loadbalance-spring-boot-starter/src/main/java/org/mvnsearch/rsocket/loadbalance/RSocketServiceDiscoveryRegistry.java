package org.mvnsearch.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RSocketServiceDiscoveryRegistry implements RSocketServiceRegistry {
    /**
     * 应用名称和应用对应的最新地址列表
     */
    private final Map<String, Sinks.Many<List<RSocketServerInstance>>> service2Servers = new ConcurrentHashMap<>();

    private final Map<String, List<RSocketServerInstance>> snapshots = new HashMap<>();
    @Autowired
    private ReactiveDiscoveryClient discoveryClient;
    private boolean refreshing = false;

    @Scheduled(fixedRate = 5000)
    public void refreshServers() {
        if (!refreshing) {
            refreshing = true;
            try {
                System.out.println("Refresh server now");
            } finally {
                refreshing = false;
            }
        }
    }

    public void setServers(String serviceName, List<RSocketServerInstance> servers) {
        String appName = convertToAppName(serviceName);
        if (service2Servers.containsKey(appName)) {
            this.service2Servers.get(appName).tryEmitNext(servers);
            this.snapshots.put(appName, servers);
        }
    }

    @Override
    public RSocketRequester buildLoadBalanceRSocket(String serviceName, RSocketRequester.Builder builder) {
        return builder.transports(this.getServers(serviceName), new RoundRobinLoadbalanceStrategy());
    }

    public Flux<List<LoadbalanceTarget>> getServers(String serviceName) {
        final String appName = convertToAppName(serviceName);
        if (!service2Servers.containsKey(appName)) {
            service2Servers.put(appName, Sinks.many().replay().latest());
            return Flux.from(discoveryClient.getInstances(appName)
                    .map(serviceInstance -> {
                        String host = serviceInstance.getHost();
                        String rsocketPort = serviceInstance.getMetadata().getOrDefault("rsocketPort", "6565");
                        return new RSocketServerInstance(host, Integer.parseInt(rsocketPort));
                    })
                    .collectList()
                    .doOnNext(rSocketServerInstances -> {
                        snapshots.put(appName, rSocketServerInstances);
                        service2Servers.get(appName).tryEmitNext(rSocketServerInstances);
                    }))
                    .thenMany(service2Servers.get(appName).asFlux().map(this::toLoadBalanceTarget));
        }
        return service2Servers.get(appName)
                .asFlux()
                .map(this::toLoadBalanceTarget);
    }

    private List<LoadbalanceTarget> toLoadBalanceTarget(List<RSocketServerInstance> rSocketServers) {
        return rSocketServers.stream()
                .map(server -> LoadbalanceTarget.from(server.getHost() + server.getPort(), server.constructClientTransport()))
                .collect(Collectors.toList());
    }

    private String convertToAppName(String serviceName) {
        String appName = serviceName.replaceAll("\\.", "-");
        if (appName.contains("-")) {
            String temp = appName.substring(appName.lastIndexOf("-") + 1);
            //如果首字母大写，则表示为服务接口名称
            if (Character.isUpperCase(temp.toCharArray()[0])) {
                appName = appName.substring(0, appName.lastIndexOf("-"));
            }
        }
        return appName;
    }

}
