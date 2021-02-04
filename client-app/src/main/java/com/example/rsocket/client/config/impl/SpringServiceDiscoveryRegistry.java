package com.example.rsocket.client.config.impl;

import com.example.rsocket.client.config.RSocketServerInstance;
import com.example.rsocket.client.config.RSocketServiceRegistry;
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
    /**
     * 应用名称和应用对应的最新地址列表
     */
    private Map<String, Sinks.Many<List<RSocketServerInstance>>> service2Servers = new ConcurrentHashMap<>();

    @Autowired
    private ReactiveDiscoveryClient discoveryClient;

    public void setServers(String serviceName, List<RSocketServerInstance> servers) {
        String appName = convertToAppName(serviceName);
        if (service2Servers.containsKey(appName)) {
            this.service2Servers.get(appName).tryEmitNext(servers);
        }
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
                    .doOnNext(rSocketServerInstances -> service2Servers.get(appName).tryEmitNext(rSocketServerInstances)))
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
