package github.keboom.enumration;

/**
 * @author keboom
 * @date 2021/4/11 12:44
 */
public enum RpcConfigProperties {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;


    RpcConfigProperties(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}
