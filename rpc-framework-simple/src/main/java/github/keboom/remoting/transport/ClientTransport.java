package github.keboom.remoting.transport;

import github.keboom.remoting.dto.RpcRequest;

/**
 * @author keboom
 * @date 2021/3/22 19:28
 */
public interface ClientTransport {


    Object sendRpcRequest(RpcRequest rpcRequest);

}
