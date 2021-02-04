RSocket Load Balancing with Spring Cloud Registry
=================================================

RSocket load balance based on Spring Cloud Service Registry.

![LoadBalance Structure](./loadbalance-structure.png)

# How to run?

* Start Consul first： `docker-compose up -d` ，then open http://localhost:8500
* Start server-app: `mvn -pl server-app spring-boot:run`
* Start client-app: `mvn -pl client-app spring-boot:run`
* Test your RSocket service invocation： `curl http://localhost:9080/square/3`

# App & Service interface naming specification
Spring Cloud Service Registry uses `spring.application.name` as service name on registry server, and appName is the serviceId argument in `ReactiveDiscoveryClient.getInstances(String serviceId);`

For example, we have a service app with two service interfaces: MathCalculatorService and ExchangeCalculatorService.  
Please use Java package naming style to name your app, such as `com-example-calculator`.
Service interface naming should follow `String serviceName = appName.replace("-", ".") + "." + interfaceName; ` rule, example as following:

* com.example.calculator.MathCalculatorService
* com.example.calculator.ExchangeCalculatorService

Why this naming style?  Take a look at the following steps to call remote RSocket services:

* Extract appName from service full name. For example, appName is `com-example-calculator`  from `com.example.calculator.MathCalculatorService`
* Invoke `ReactiveDiscoveryClient.getInstances(appName)` to get app instance list
* Build RSocketRequester with load balance support with `RSocketRequester.Builder.transports(servers)`
* Call RSocketRequester api with service full name as routing key

```
 rsocketRequester.route("com.example.calculator.MathCalculatorService.square")
                .data(number)
                .retrieveMono(Integer.class)
```

This naming style is easy for RSocket to interact with service registry and RSocket service routing.

# References

* Spring Cloud Consul: https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/
* Spring Retrosocket: https://github.com/spring-projects-experimental/spring-retrosocket
* RSocket Load Balancing: https://www.vinsguru.com/rsocket-load-balancing-client-side/
