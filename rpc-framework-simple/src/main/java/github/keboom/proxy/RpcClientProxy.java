package github.keboom.proxy;

import github.keboom.entity.RpcServiceProperties;
import github.keboom.remoting.dto.RpcMessageChecker;
import github.keboom.remoting.dto.RpcRequest;
import github.keboom.remoting.dto.RpcResponse;
import github.keboom.remoting.transport.ClientTransport;
import github.keboom.remoting.transport.netty.client.NettyClientTransport;
import github.keboom.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author keboom
 * @date 2021/3/13 17:34
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private final ClientTransport clientTransport;
    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy(ClientTransport clientTransport, RpcServiceProperties rpcServiceProperties) {
        this.clientTransport = clientTransport;
        if (rpcServiceProperties.getGroup() == null) {
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null) {
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }

    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.rpcServiceProperties = RpcServiceProperties.builder().group("").version("").build();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse<Object>> completableFuture = (CompletableFuture<RpcResponse<Object>>) clientTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        }
        if (clientTransport instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse<Object>) clientTransport.sendRpcRequest(rpcRequest);
        }
        //校验 RpcResponse 和 RpcRequest
        RpcMessageChecker.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }
}
