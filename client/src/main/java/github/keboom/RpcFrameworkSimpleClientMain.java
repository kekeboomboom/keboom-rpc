package github.keboom;

import github.keboom.remoting.transport.ClientTransport;
import github.keboom.proxy.RpcClientProxy;
import github.keboom.remoting.transport.socket.SocketRpcClient;

/**
 * @author keboom
 * @date 2021/3/13 17:33
 */
public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new SocketRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService helloservice = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloservice.hello(new Hello("111", "222"));
        System.out.println(hello);

    }
}
