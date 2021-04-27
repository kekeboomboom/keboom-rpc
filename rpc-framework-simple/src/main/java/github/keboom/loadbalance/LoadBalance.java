package github.keboom.loadbalance;

import java.util.List;

/**
 * @author keboom
 * @date 2021/4/11 11:26
 */
public interface LoadBalance {
    /**
     * 在已有服务提供地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName);
}
