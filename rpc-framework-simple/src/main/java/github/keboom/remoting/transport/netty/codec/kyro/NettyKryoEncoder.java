package github.keboom.remoting.transport.netty.codec.kyro;

import github.keboom.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * @author keboom
 * @date 2021/3/17 19:48
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)) {
            byte[] body = serializer.serialize(o);
            int dataLength = body.length;
            byteBuf.writeInt(dataLength);
            byteBuf.writeBytes(body);
        }
    }
}
