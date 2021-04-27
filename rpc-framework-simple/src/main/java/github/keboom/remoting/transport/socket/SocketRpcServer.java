package github.keboom.remoting.transport.socket;

import github.keboom.config.CustomShutdownHook;
import github.keboom.provider.ServiceProvider;
import github.keboom.provider.ServiceProviderImpl;
import github.keboom.registry.ServiceRegistry;
import github.keboom.registry.zk.ZkServiceRegistry;
import github.keboom.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author keboom
 * @date 2021/3/13 20:17
 */
@Slf4j
public class SocketRpcServer {

    private ExecutorService threadPool;
    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket();) {
            server.bind(new InetSocketAddress(host, port));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            log.info("server starts...");
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException", e);
        }
    }
}
