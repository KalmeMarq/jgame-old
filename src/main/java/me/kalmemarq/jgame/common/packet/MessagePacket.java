package me.kalmemarq.jgame.common.packet;

import java.time.Instant;

public class MessagePacket extends Packet {
    private String username;
    private Instant createdTime;
    private String message;

    public MessagePacket() {
    }

    public MessagePacket(String username, Instant createdTime, String message) {
        this.username = username;
        this.createdTime = createdTime;
        this.message = message;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeString(this.username);
        buffer.writeLong(this.createdTime.toEpochMilli());
        buffer.writeString(this.message);
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.username = buffer.readString();
        this.createdTime = Instant.ofEpochMilli(buffer.readLong());
        this.message = buffer.readString();
    }

    @Override
    public void apply(PacketListener packetListener) {
        packetListener.onMessagePacket(this);
    }

    public String getUsername() {
        return this.username;
    }

    public Instant getCreatedTime() {
        return this.createdTime;
    }

    public String getMessage() {
        return this.message;
    }
}
