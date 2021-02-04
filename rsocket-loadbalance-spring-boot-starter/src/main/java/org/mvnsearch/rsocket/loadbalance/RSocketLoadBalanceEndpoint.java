package org.mvnsearch.rsocket.loadbalance;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Endpoint(id = "rsocketlb")
public class RSocketLoadBalanceEndpoint {
    private final RSocketServiceRegistry rsocketServiceRegistry;

    public RSocketLoadBalanceEndpoint(RSocketServiceRegistry rsocketServiceRegistry) {
        this.rsocketServiceRegistry = rsocketServiceRegistry;
    }

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        Map<String, List<String>> services = new HashMap<>();
        for (Map.Entry<String, List<RSocketServerInstance>> entry : rsocketServiceRegistry.getSnapshots().entrySet()) {
            services.put(entry.getKey(), entry.getValue().stream().map(RSocketServerInstance::getURI).collect(Collectors.toList()));
        }
        info.put("services", services);
        info.put("lastRefreshAt", rsocketServiceRegistry.getLastRefreshTimestamp());
        return info;
    }
}
