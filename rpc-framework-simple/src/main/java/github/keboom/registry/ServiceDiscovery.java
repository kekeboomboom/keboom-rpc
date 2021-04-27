package github.keboom.registry;

import java.net.InetSocketAddress;

/**
 * @author keboom
 * @date 2021/3/31 21:35
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(String serviceName);
}
