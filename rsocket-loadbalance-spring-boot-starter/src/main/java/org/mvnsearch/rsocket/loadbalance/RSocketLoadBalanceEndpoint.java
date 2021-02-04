package org.mvnsearch.rsocket.loadbalance;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.Map;

@Endpoint(id = "rsocketlb")
public class RSocketLoadBalanceEndpoint {

    private RSocketServiceRegistry rsocketServiceRegistry;

    public RSocketLoadBalanceEndpoint(RSocketServiceRegistry rsocketServiceRegistry) {
        this.rsocketServiceRegistry = rsocketServiceRegistry;
    }

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("services", rsocketServiceRegistry.getSnapshots());
        info.put("lastRefreshAt", rsocketServiceRegistry.getLastRefreshTimestamp());
        return info;
    }
}
