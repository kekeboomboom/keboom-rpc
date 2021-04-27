package github.keboom.remoting.transport.socket;

import github.keboom.remoting.dto.RpcRequest;
import github.keboom.exception.RpcException;
import github.keboom.registry.ServiceDiscovery;
import github.keboom.registry.zk.ZkServiceDiscovery;
import github.keboom.remoting.transport.ClientTransport;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author keboom
 * @date 2021/3/13 18:07
 */
@AllArgsConstructor
public class SocketRpcClient implements ClientTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        this.serviceDiscovery = new ZkServiceDiscovery();
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败", e);
        }
    }

}
