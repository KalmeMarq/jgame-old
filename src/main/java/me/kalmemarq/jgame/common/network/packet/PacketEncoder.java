package me.kalmemarq.jgame.common.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        int packetId = Packet.PACKETS.indexOf(packet.getClass());

        if (packetId == -1) {
            throw new IOException("Could not find id of packet '" + packet.getClass().getSimpleName() + "'");
        }

        PacketByteBuf packetBuf = new PacketByteBuf(out);
        packetBuf.writeVarInt(packetId);
        packet.write(packetBuf);
    }
}
