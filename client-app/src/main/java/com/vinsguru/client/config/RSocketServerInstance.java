package com.vinsguru.client.config;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RSocketServerInstance {
    private String host;
    private int port;

    public ClientTransport constructClientTransport() {
        return TcpClientTransport.create(host, port);
    }
}
