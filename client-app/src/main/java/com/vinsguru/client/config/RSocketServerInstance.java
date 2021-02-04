package com.vinsguru.client.config;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;

public class RSocketServerInstance {
    private String host;
    private int port;

    public RSocketServerInstance() {
    }

    public RSocketServerInstance(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ClientTransport constructClientTransport() {
        return TcpClientTransport.create(host, port);
    }
}
