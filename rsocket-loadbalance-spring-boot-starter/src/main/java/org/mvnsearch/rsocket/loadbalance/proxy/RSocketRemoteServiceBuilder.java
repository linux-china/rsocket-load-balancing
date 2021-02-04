package org.mvnsearch.rsocket.loadbalance.proxy;

import org.springframework.messaging.rsocket.RSocketRequester;

import java.lang.reflect.Proxy;

public class RSocketRemoteServiceBuilder<T> {
    private String serviceName;
    private Class<?> serviceInterface;
    private RSocketRequester rsocketRequester;

    public RSocketRemoteServiceBuilder<T> serviceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public RSocketRemoteServiceBuilder<T> rsocketRequester(RSocketRequester rsocketRequester) {
        this.rsocketRequester = rsocketRequester;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        RSocketRemoteCallInvocationHandler handler = new RSocketRemoteCallInvocationHandler(rsocketRequester, serviceName, serviceInterface);
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                handler);
    }

}
