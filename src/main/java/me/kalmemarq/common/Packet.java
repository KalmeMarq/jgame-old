package me.kalmemarq.common;

import java.util.List;

public abstract class Packet {
    public static final List<Class<? extends Packet>> PACKETS = List.of(
            MessagePacket.class
    );

    abstract public void write(PacketByteBuf buffer);
    abstract public void read(PacketByteBuf buffer);

    abstract public void apply(PacketListener packetListener);

    public interface PacketListener {
        void onMessagePacket(MessagePacket packet);
        void onDisconnectPacket(DisconnectPacket packet);

        void onDisconnected();
    }
}
