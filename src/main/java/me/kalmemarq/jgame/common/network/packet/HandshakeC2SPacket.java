package me.kalmemarq.jgame.common.network.packet;

public class HandshakeC2SPacket extends Packet {
    private int protocolVersion;
    
    public HandshakeC2SPacket() {
    }
    
    public HandshakeC2SPacket(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeInt(this.protocolVersion);
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.protocolVersion = buffer.readInt();
    }

    @Override
    public void apply(PacketListener packetListener) {
        packetListener.onHandshakeC2SPacket(this);
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }
}
