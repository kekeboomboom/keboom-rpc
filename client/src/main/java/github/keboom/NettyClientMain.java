package github.keboom;


import github.keboom.entity.RpcServiceProperties;
import github.keboom.remoting.transport.ClientTransport;
import github.keboom.proxy.RpcClientProxy;
import github.keboom.remoting.transport.netty.client.NettyClientTransport;

/**
 * @author keboom
 * @date 2021/3/17 18:17
 */
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test1").version("version1").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            helloService.hello(new Hello("111", "222"));
        }
    }

}
