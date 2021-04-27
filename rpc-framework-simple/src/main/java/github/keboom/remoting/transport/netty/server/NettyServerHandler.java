package github.keboom.remoting.transport.netty.server;

import github.keboom.enumration.RpcMessageType;
import github.keboom.remoting.dto.RpcRequest;
import github.keboom.remoting.dto.RpcResponse;
import github.keboom.remoting.handler.RpcRequestHandler;
import github.keboom.factory.SingletonFactory;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author keboom
 * @date 2021/3/17 21:45
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        log.info(String.format("server handle message from client by thread: %s", Thread.currentThread().getName()));
        try {
            log.info("server receive msg: [{}] ", msg);
            RpcRequest rpcRequest = (RpcRequest) msg;
            if (rpcRequest.getRpcMessageType() == RpcMessageType.HEART_BEAT) {
                log.info("receive heat beat msg from client");
                return;
            }
            Object result = rpcRequestHandler.handler(rpcRequest);
            log.info(String.format("server get result:%s", result.toString()));
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                log.error("not writable now, message dropped");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
