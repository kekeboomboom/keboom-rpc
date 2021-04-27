package github.keboom;

import github.keboom.entity.RpcServiceProperties;
import github.keboom.provider.ServiceProvider;
import github.keboom.provider.ServiceProviderImpl;
import github.keboom.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author keboom
 * @date 2021/4/11 12:13
 */
public class NettyServerMain2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.start();
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test").version("1").build();
        serviceProvider.publishService(helloService, rpcServiceProperties);
    }
}
