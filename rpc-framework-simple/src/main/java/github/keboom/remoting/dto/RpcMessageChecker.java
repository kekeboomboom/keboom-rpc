package github.keboom.remoting.dto;

import github.keboom.enumration.RpcErrorMessage;
import github.keboom.enumration.RpcResponseCode;
import github.keboom.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author keboom
 * @date 2021/3/20 20:56
 */
@Slf4j
public class RpcMessageChecker {
    public static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker() {

    }

    public static void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
