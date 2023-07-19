package me.kalmemarq.jgame.common.network.packet;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PacketByteBuf {
    private final ByteBuf buffer;

    public PacketByteBuf(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void writeByte(int value) {
        this.buffer.writeByte(value);
    }

    public byte readByte() {
        return this.buffer.readByte();
    }

    public void writeShort(int value) {
        this.buffer.writeShort(value);
    }

    public short readShort() {
        return this.buffer.readShort();
    }

    public void writeInt(int value) {
        this.buffer.writeInt(value);
    }

    public int readInt() {
        return this.buffer.readInt();
    }

    public void writeLong(long value) {
        this.buffer.writeLong(value);
    }

    public long readLong() {
        return this.buffer.readLong();
    }

    public void writeFloat(float value) {
        this.buffer.writeFloat(value);
    }

    public float readFloat() {
        return this.buffer.readFloat();
    }

    public void writeDouble(double value) {
        this.buffer.writeDouble(value);
    }

    public double readDouble() {
        return this.buffer.readDouble();
    }

    public void writeString(String value) {
        this.writeString(value, StandardCharsets.UTF_8);
    }

    public void writeString(String value, Charset charset) {
        byte[] bs = value.getBytes(charset);
        this.writeVarInt(bs.length);
        this.buffer.writeBytes(bs);
    }

    public String readString() {
        return this.readString(StandardCharsets.UTF_8);
    }

    public String readString(Charset charset) {
        int length = this.readVarInt();
        String value = this.buffer.toString(this.buffer.readerIndex(), length, charset);
        this.buffer.readerIndex(this.buffer.readerIndex() + length);
        return value;
    }

    public void writeVarInt(int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                this.writeByte(value);
                return;
            }
            this.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }

    public int readVarInt() {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = this.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt is too big");
        } while ((b & 0x80) == 128);
        return i;
    }
}
