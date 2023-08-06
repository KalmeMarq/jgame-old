package me.kalmemarq.jgame.client.render;

public class Tessellator {
    private static final Tessellator INSTANCE = new Tessellator(0x20000);

    public static Tessellator getInstance() {
        return Tessellator.INSTANCE;
    }

    private final BufferBuilder bufferBuilder;

    public Tessellator(int capacity) {
        this.bufferBuilder = new BufferBuilder(capacity);
    }

    public BufferBuilder getBufferBuilder() {
        return this.bufferBuilder;
    }

    public void draw() {
        BufferBuilder.BuiltBuffer builtBuffer = this.bufferBuilder.end();
        builtBuffer.format().getVertexBuffer().bind();
        builtBuffer.format().getVertexBuffer().upload(builtBuffer);
        builtBuffer.format().getVertexBuffer().draw();
    }
}
