package github.keboom;

import github.keboom.remoting.transport.socket.SocketRpcServer;

/**
 * @author keboom
 * @date 2021/3/13 20:51
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer("127.0.0.1", 9999);
        socketRpcServer.publishService(helloService, HelloService.class);
    }
}
