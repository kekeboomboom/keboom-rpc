package github.keboom.remoting.dto;

import github.keboom.enumration.RpcMessageType;
import lombok.*;

import java.io.Serializable;

/**
 * @author keboom
 * @date 2021/3/13 17:40
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 6471261232768359207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private RpcMessageType rpcMessageType;
    private String version;
    private String group;
}
