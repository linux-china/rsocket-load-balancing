package org.mvnsearch.rsocket.loadbalance.proxy;

import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RSocketRemoteCallInvocationHandler implements InvocationHandler {
    private final RSocketRequester rsocketRequester;
    private final Class<?> serviceInterface;
    private final String serviceName;
    private final static Map<Method, Class<?>> methodReturnTypeMap = new HashMap<>();

    public RSocketRemoteCallInvocationHandler(RSocketRequester rsocketRequester, String serviceName, Class<?> serviceInterface) {
        this.rsocketRequester = rsocketRequester;
        this.serviceName = serviceName;
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            return DefaultMethodHandler.getMethodHandle(method, serviceInterface).bindTo(proxy).invokeWithArguments(args);
        }
        String methodName = method.getName();
        Object arg = null;
        if (args != null && args.length > 0) {
            arg = args[0];
        }
        Class<?> returnType = methodReturnTypeMap.get(method);
        if (returnType == null) {
            returnType = parseInferredClass(method.getGenericReturnType());
            methodReturnTypeMap.put(method, returnType);
        }
        RSocketRequester.RequestSpec requestSpec = rsocketRequester.route(serviceName + "." + methodName);
        RSocketRequester.RetrieveSpec retrieveSpec;
        if (arg != null) {
            retrieveSpec = requestSpec.data(arg);
        } else {
            retrieveSpec = requestSpec;
        }
        // Flux return type: request/stream or channel
        if (method.getReturnType().isAssignableFrom(Flux.class)) {
            return retrieveSpec.retrieveFlux(returnType);
        } else { //Mono return type
            // Void return type: fireAndForget
            if (returnType.equals(Void.class)) {
                return retrieveSpec.send();
            } else { // request/response
                return requestSpec.retrieveMono(returnType);
            }
        }
    }


    public static Class<?> parseInferredClass(Type genericType) {
        Class<?> inferredClass = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericType;
            Type[] typeArguments = type.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof ParameterizedType) {
                    inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                } else if (typeArgument instanceof Class) {
                    inferredClass = (Class<?>) typeArgument;
                } else {
                    String typeName = typeArgument.getTypeName();
                    if (typeName.contains(" ")) {
                        typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
                    }
                    if (typeName.contains("<")) {
                        typeName = typeName.substring(0, typeName.indexOf("<"));
                    }
                    try {
                        inferredClass = Class.forName(typeName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (inferredClass == null && genericType instanceof Class) {
            inferredClass = (Class<?>) genericType;
        }
        return inferredClass;
    }
}
