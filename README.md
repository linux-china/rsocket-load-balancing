RSocket Load Balancing
======================

基于Spring Cloud Consul服务注册发现的RSocket负载均衡，架构如下：

![LoadBalance Structure](./loadbalance-structure.png)

# 如何运行?

* 首先启动Consul： `docker-compose up -d` ，然后访问 http://localhost:8500
* 启动server-app: `mvn -pl server-app spring-boot:run`
* 启动client-app: `mvn -pl client-app spring-boot:run`
* 测试服务： `curl http://localhost:9080/square/3`

# References

* https://www.vinsguru.com/rsocket-load-balancing-client-side/
