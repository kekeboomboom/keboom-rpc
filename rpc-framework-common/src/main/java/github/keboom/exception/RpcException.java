package github.keboom.exception;

import github.keboom.enumration.RpcErrorMessage;

/**
 * @author keboom
 * @date 2021/3/13 22:47
 */
public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessage rpcErrorMessage, String detail) {
        super(rpcErrorMessage.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessage) {
        super(rpcErrorMessage.getMessage());
    }
}

