package github.keboom.registry;

import java.net.InetSocketAddress;

/**
 * @author keboom
 * @date 2021/3/14 20:14
 */
public interface ServiceRegistry {
    /**
     *
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);

}
