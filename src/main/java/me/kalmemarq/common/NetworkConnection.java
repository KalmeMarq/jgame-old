package me.kalmemarq.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import me.kalmemarq.common.packet.Packet;
import me.kalmemarq.common.packet.PacketCallbacks;
import me.kalmemarq.common.packet.PacketDecoder;
import me.kalmemarq.common.packet.PacketEncoder;

import java.net.SocketAddress;

public class NetworkConnection extends SimpleChannelInboundHandler<Packet> {
    private Channel channel;
    private SocketAddress address;
    private Packet.PacketListener packetListener;

    public void setPacketListener(Packet.PacketListener packetListener) {
        this.packetListener = packetListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        this.address = this.channel.remoteAddress();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        if (this.isOpen()) {
            if (this.packetListener != null) {
                packet.apply(this.packetListener);
            }
        }
    }

    public void sendPacket(Packet packet) {
        this.sendPacket(packet, null);
    }

    public void sendPacket(Packet packet, PacketCallbacks callbacks) {
        if (this.isOpen()) {
            ChannelFuture channelFuture = this.channel.writeAndFlush(packet);

            if (callbacks != null) {
                channelFuture.addListener(ft -> {
                    if (ft.isSuccess()) {
                        callbacks.onSuccess();
                    } else {
                        callbacks.onFailure();
                        Packet failurePacket = callbacks.getFailurePacket();
                        if (failurePacket != null) {
                            ChannelFuture failureChannelFuture = this.channel.writeAndFlush(failurePacket);
                            failureChannelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                        }
                    }
                });
            }

            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    public void handleDisconnection() {
        if (this.channel.isOpen()) return;

        if (this.packetListener != null) {
            this.packetListener.onDisconnected();
        }
    }

    public void disconnect() {
        if (this.isOpen()) {
            this.channel.close().syncUninterruptibly();
        }
    }

    public boolean isOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public static void addHandlers(ChannelPipeline pipeline) {
        pipeline.addLast("encoder", new PacketEncoder()).addLast("decoder", new PacketDecoder());
    }
}
