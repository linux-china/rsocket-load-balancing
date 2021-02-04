package org.mvnsearch.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Date;
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
    private ReactiveDiscoveryClient discoveryClient;
    private Date lastRefreshTimeStamp = new Date();
    private boolean refreshing = false;

    public RSocketServiceDiscoveryRegistry(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Map<String, List<RSocketServerInstance>> getSnapshots() {
        return this.snapshots;
    }

    @Override
    public Date getLastRefreshTimestamp() {
        return this.lastRefreshTimeStamp;
    }

    @Scheduled(initialDelay = 5000, fixedRate = 15000)
    public void refreshServers() {
        if (!refreshing) {
            refreshing = true;
            lastRefreshTimeStamp = new Date();
            try {
                if (!snapshots.isEmpty()) {
                    for (String serviceName : service2Servers.keySet()) {
                        discoveryClient.getInstances(serviceName)
                                .map(this::convertToRSocketServerInstance)
                                .collectList().subscribe(newServiceInstances -> {
                            List<RSocketServerInstance> currentServerInstances = snapshots.get(serviceName);
                            //not same
                            if (!(currentServerInstances.size() == newServiceInstances.size() && currentServerInstances.containsAll(newServiceInstances))) {
                                System.out.println("Begin to refresh upstream RSocket servers");
                                setServers(serviceName, newServiceInstances);
                            }
                        });
                    }
                }
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
                    .map(this::convertToRSocketServerInstance)
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
        // service with assigned name
        if (serviceName.contains(":")) {
            return appName.substring(0, appName.indexOf(":"));
        }
        if (appName.contains("-")) {
            String temp = appName.substring(appName.lastIndexOf("-") + 1);
            // if first character is uppercase, and it means service name
            if (Character.isUpperCase(temp.toCharArray()[0])) {
                appName = appName.substring(0, appName.lastIndexOf("-"));
            }
        }
        return appName;
    }

    private RSocketServerInstance convertToRSocketServerInstance(ServiceInstance serviceInstance) {
        RSocketServerInstance serverInstance = new RSocketServerInstance();
        serverInstance.setHost(serviceInstance.getHost());
        serverInstance.setSchema(serviceInstance.getMetadata().getOrDefault("rsocketSchema", "tcp"));
        if (serverInstance.isWebSocket()) {
            serverInstance.setPort(serviceInstance.getPort());
            serverInstance.setPath(serviceInstance.getMetadata().getOrDefault("rsocketPath", "/rsocket"));
        } else {
            serverInstance.setPort(Integer.parseInt(serviceInstance.getMetadata().getOrDefault("rsocketPort", "42252")));
        }
        return serverInstance;
    }

}
