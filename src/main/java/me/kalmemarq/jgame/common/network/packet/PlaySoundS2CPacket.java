package me.kalmemarq.jgame.common.network.packet;

public class PlaySoundS2CPacket extends Packet {
    private String path;
    private float volume;
    private float pitch;

    public PlaySoundS2CPacket() {
    }

    public PlaySoundS2CPacket(String path, float volume, float pitch) {
        this.path = path;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeString(this.path);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.path = buffer.readString();
        this.volume = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public void apply(PacketListener packetListener) {
        packetListener.onPlaySoundPacket(this);
    }

    public String getPath() {
        return this.path;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }
}
