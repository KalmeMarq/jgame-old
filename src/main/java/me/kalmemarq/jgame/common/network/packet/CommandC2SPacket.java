package me.kalmemarq.jgame.common.network.packet;
public class CommandC2SPacket extends Packet {
    private String command;

    public CommandC2SPacket() {
    }

    public CommandC2SPacket(String command) {
        this.command = command;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeString(this.command);
    }

    @Override
    public void read(PacketByteBuf buffer) {
        this.command = buffer.readString();
    }

    @Override
    public void apply(PacketListener packetListener) {
        packetListener.onCommandC2SPacket(this);
    }

    public String getCommand() {
        return this.command;
    }
}
