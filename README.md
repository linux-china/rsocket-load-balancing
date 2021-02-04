RSocket Load Balancing with Spring Cloud Registry
=================================================

基于Spring Cloud Consul服务注册发现的RSocket负载均衡，架构如下：

![LoadBalance Structure](./loadbalance-structure.png)

# 如何运行?

* 首先启动Consul： `docker-compose up -d` ，然后访问 http://localhost:8500
* 启动server-app: `mvn -pl server-app spring-boot:run`
* 启动client-app: `mvn -pl client-app spring-boot:run`
* 测试服务： `curl http://localhost:9080/square/3`


# 服务命名规范
Spring Cloud的注册发现机制是基于`spring.application.name`，也就是后续的服务查找就是基于该名称进行的。 应用名称不能包含"."，这个不是合法的DNS主机名，会被转换为"-"。

假设你有一个服务应用，名称为calculator，同时提供两个服务: 数学计算器服务(MathCalculatorService)和汇率计算服务(ExchangeCalculatorService),
那么你的应用名则为 com-example-calculator，请确保命名空间(calculator)和其他人不冲突。

对应的服务命名则为如下：

* com.example.calculator.MathCalculatorService
* com.example.calculator.ExchangeCalculatorService

请确保应用名和服务名之间不要有其他字符串，这个约定主要是方便服务发现查找服务。

# 客户端调用

假设客户端要调用 com.example.calculator.LoanCalculatorService 服务，流程如下：

* 根据 "com-example-calculator" 应用名，查找对应的服务地址列表
* 创建和 "com-example-calculator" 应用对应服务器列表的连接
* 根据 "com.example.calculator.LoanCalculatorService.xxx" 路由发送服务给服务提供方
* 接收服务方返回的结果。


# References

* Spring Cloud Consul: https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/
* RSocket Load Balancing: https://www.vinsguru.com/rsocket-load-balancing-client-side/
