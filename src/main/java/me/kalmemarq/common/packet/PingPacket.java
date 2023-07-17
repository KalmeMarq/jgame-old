package me.kalmemarq.common.packet;

public class PingPacket extends Packet {
    private long nanoTime;

    public PingPacket() {
    }

    public PingPacket(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeLong(this.nanoTime);
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.nanoTime = buffer.readLong();
    }

    @Override
    public void apply(PacketListener listener) {
        listener.onPingPacket(this);
    }

    public long getNanoTime() {
        return this.nanoTime;
    }
}
