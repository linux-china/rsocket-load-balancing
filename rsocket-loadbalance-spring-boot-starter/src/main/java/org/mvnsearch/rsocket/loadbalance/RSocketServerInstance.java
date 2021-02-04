package org.mvnsearch.rsocket.loadbalance;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;

import java.net.URI;

public class RSocketServerInstance {
    private String host;
    private int port;
    /**
     * schema, such as tcp, ws, wss
     */
    private String schema = "tcp";
    /**
     * path, for websocket only
     */
    private String path;

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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isWebSocket() {
        return "ws".equals(this.schema) || "wss".equals(this.schema);
    }
    
    public String getURI() {
        if(isWebSocket())  {
            return schema + "://" + host + ":" + port + path;
        }   else {
            return schema +"://"+host+":"+port;
        }
    }

    public ClientTransport constructClientTransport() {
        if (this.isWebSocket()) {
            return WebsocketClientTransport.create(URI.create(getURI()));
        }
        return TcpClientTransport.create(host, port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RSocketServerInstance that = (RSocketServerInstance) o;
        if (port != that.port) return false;
        return host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
