package github.keboom.registy;

import github.keboom.registry.ServiceDiscovery;
import github.keboom.registry.ServiceRegistry;
import github.keboom.registry.zk.ZkServiceDiscovery;
import github.keboom.registry.zk.ZkServiceRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;

/**
 * @author keboom
 * @date 2021/3/29 18:42
 */
public class ZkServiceRegistryTest {

    @Test
    void should_register_service_successful_and_lookup_service_by_service_name() {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        zkServiceRegistry.registerService("github.keboom.registry.zk.ZkServiceRegistry",givenInetSocketAddress);
        ServiceDiscovery zkServiceDiscovery = new ZkServiceDiscovery();
        InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService("github.keboom.registry.zk.ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());

    }
}
