package com.example.rsocket.client.proxy;

import org.springframework.messaging.rsocket.RSocketRequester;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RSocketRemoteCallInvocationHandler implements InvocationHandler {

    private final RSocketRequester rsocketRequester;
    private final String serviceName;
    private final static Map<Method, Class<?>> methodReturnTypeMap = new HashMap<>();

    public RSocketRemoteCallInvocationHandler(RSocketRequester rsocketRequester, String serviceName) {
        this.rsocketRequester = rsocketRequester;
        this.serviceName = serviceName;
    }

    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
        if (arg != null) {
            return rsocketRequester.route(serviceName + "." + methodName).data(arg).retrieveMono(returnType);
        } else {
            return rsocketRequester.route(serviceName + "." + methodName).retrieveMono(returnType);
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
