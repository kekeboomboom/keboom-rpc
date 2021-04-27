package github.keboom.remoting.handler;

import github.keboom.entity.RpcServiceProperties;
import github.keboom.factory.SingletonFactory;
import github.keboom.remoting.dto.RpcRequest;
import github.keboom.remoting.dto.RpcResponse;
import github.keboom.enumration.RpcResponseCode;
import github.keboom.exception.RpcException;
import github.keboom.provider.ServiceProvider;
import github.keboom.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author keboom
 * @date 2021/3/15 20:51
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public Object handler(RpcRequest rpcRequest) {
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().serviceName(rpcRequest.getInterfaceName())
                .version(rpcRequest.getVersion())
                .group(rpcRequest.getGroup()).build();
        Object service = serviceProvider.getServiceProvider(rpcServiceProperties);
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
