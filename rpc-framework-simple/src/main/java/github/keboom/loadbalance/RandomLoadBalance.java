package github.keboom.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author keboom
 * @date 2021/4/11 11:26
 */
public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
