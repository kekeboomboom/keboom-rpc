package github.keboom.provider;

import github.keboom.entity.RpcServiceProperties;

/**
 * @author keboom
 * @date 2021/3/23 16:12
 */
public interface ServiceProvider {

    void addServiceProvider(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    Object getServiceProvider(RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     *
     * @param service 服务实例对象
     */
    void publishService(Object service);

    void publishService(Object service, RpcServiceProperties rpcServiceProperties);
}
