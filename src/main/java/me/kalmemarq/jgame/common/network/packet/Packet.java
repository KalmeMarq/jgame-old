package me.kalmemarq.jgame.common.network.packet;

import java.util.List;

public abstract class Packet {
    public static final List<Class<? extends Packet>> PACKETS = List.of(
            MessagePacket.class,
            DisconnectPacket.class,
            PingPacket.class,
            CommandC2SPacket.class,
            PlaySoundS2CPacket.class
    );

    abstract public void write(PacketByteBuf buffer);
    abstract public void read(PacketByteBuf buffer);

    abstract public void apply(PacketListener packetListener);

    public interface PacketListener {
        void onMessagePacket(MessagePacket packet);
        void onDisconnectPacket(DisconnectPacket packet);
        void onPingPacket(PingPacket packet);
        void onPlaySoundPacket(PlaySoundS2CPacket packet);
        void onCommandC2SPacket(CommandC2SPacket packet);
        void onDisconnected();
    }
}
