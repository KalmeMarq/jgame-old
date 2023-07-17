package me.kalmemarq.common.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() == 0) return;

        PacketByteBuf packetBuf = new PacketByteBuf(in);
        int packetId = packetBuf.readVarInt();
        Class<? extends Packet> packetClass = Packet.PACKETS.get(packetId);

        if (packetClass == null) {
            throw new IOException("Unknown packet of id " + packetId);
        }

        Packet packet = packetClass.getConstructor().newInstance();
        packet.read(packetBuf);
        out.add(packet);
    }
}
