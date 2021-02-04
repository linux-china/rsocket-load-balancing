RSocket Load Balancing with Spring Cloud Registry
=================================================

基于Spring Cloud服务注册发现架构实现RSocket负载均衡，架构如下：

![LoadBalance Structure](./loadbalance-structure.png)

# 如何运行?

* 首先启动Consul： `docker-compose up -d` ，然后访问 http://localhost:8500
* 启动server-app: `mvn -pl server-app spring-boot:run`
* 启动client-app: `mvn -pl client-app spring-boot:run`
* 测试服务： `curl http://localhost:9080/square/3`


# 应用和服务命名规范
Spring Cloud的注册发现机制是基于`spring.application.name`，也就是后续的服务查找就是基于该名称进行的。  
如果你调用`ReactiveDiscoveryClient.getInstances(String serviceId);`查找服务实例列表时，这个serviceId参数其实就是Spring的应用名称。
考虑到服务注册和后续的RSocket服务路由，所以我们打算设计一个简单的命名规范。

**注意：** 应用名称不能包含"."，这个不是合法的DNS主机名，会被Service Registry转换为"-"。

假设你有一个服务应用，功能名称为calculator，同时提供两个服务: 数学计算器服务(MathCalculatorService)和汇率计算服务(ExchangeCalculatorService),
那么我们该如何如何来命名应用？ 这里我们采用Java package命名规范，如 `com-example-calculator`，这样可以确保不会和其他应用重名，另外也方便和Java Package名称进行转换。

那么服务接口应该如何命名？ 服务接口基于应用名称和interface名称构建，规则为 `String serviceName = appName.replace("-", ".") + "." + interfaceName; ` ，
如下述命名都是合乎规范的：

* com.example.calculator.MathCalculatorService
* com.example.calculator.ExchangeCalculatorService

而 `com.example.calculator.math.MathCalculatorService` 则是错误的 :x:, 因为在应用名称和接口名称之间多了`math`。

为何要采用这种命名规范？ 首先让我们看一下是如何调用远程RSocket服务的：

* 首先我们根据Service全面提取处对应的应用名称(appName)，如 `com.example.calculator.MathCalculatorService` 服务对应的appName则为`com-example-calculator`
* 调用`ReactiveDiscoveryClient.getInstances(appName)` 获取应用名对应的实例列表
* 根据`RSocketRequester.Builder.transports(servers)` 构建具有负载均衡能力的RSocketRequester
* 使用服务全称作为路由进行RSocketRequester的API调用，样例代码如下：

```
 rsocketRequester.route("com.example.calculator.MathCalculatorService.square")
                .data(number)
                .retrieveMono(Integer.class)
```

通过该种命名方式，我们可以从服务全称中提取中应用名，然后和服务注册中心交互查找对应的实例列表，然后建立和服务提供者的连接，最后基于服务名称进行服务调用。


# References

* Spring Cloud Consul: https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/
* RSocket Load Balancing: https://www.vinsguru.com/rsocket-load-balancing-client-side/
