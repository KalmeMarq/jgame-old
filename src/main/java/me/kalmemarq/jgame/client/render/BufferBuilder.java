package me.kalmemarq.jgame.client.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferBuilder implements VertexConsumer {
    private boolean building;
    private final ByteBuffer buffer;
    private VertexFormat format;
    private DrawMode mode;
    private VertexFormatAttribute currentAttribute;
    private int currentAttributeId;
    private int attributeOffset;
    private int vertexCount;
    
    public BufferBuilder(int capacity) {
        this.buffer = GLAllocationUtil.allocateByteBuffer(capacity);
    }

    public void begin(VertexFormat format) {
        this.begin(DrawMode.QUADS, format);
    }

    public void begin(DrawMode mode, VertexFormat format) {
        if (this.building) {
            throw new IllegalStateException("Already building");
        }
        this.building = true;
        this.mode = mode;
        this.format = format;
        this.currentAttributeId = 0;
        this.currentAttribute = format.attributes[0];
        this.attributeOffset = 0;
        this.vertexCount = 0;
        this.buffer.rewind();
    }

    public void next() {
        if (this.currentAttributeId != 0) {
            throw new IllegalStateException("Vertex has attributes still not filled");
        }
        ++this.vertexCount;
    }

    private void nextAttribute() {
        this.attributeOffset += this.currentAttribute.byteSize;
        this.currentAttributeId = (this.currentAttributeId + 1) % this.format.attributes.length;
        this.currentAttribute = this.format.attributes[this.currentAttributeId];
    }

    @Override
    public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        return this.vertex(x, y, z);
    }

    public BufferBuilder vertex(Matrix4f matrix, double x, double y, double z) {
        return this.vertex(x, y, z);
    }

    public BufferBuilder vertex(double x, double y, double z) {
        if (this.currentAttribute != VertexFormatAttribute.POSITION) return this;
        this.buffer.putFloat(this.attributeOffset, (float) x);
        this.buffer.putFloat(this.attributeOffset + 4, (float) y);
        this.buffer.putFloat(this.attributeOffset + 8, (float) z);
        this.nextAttribute();
        return this;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        if (this.currentAttribute != VertexFormatAttribute.POSITION) return this;
        this.buffer.putFloat(this.attributeOffset, x);
        this.buffer.putFloat(this.attributeOffset + 4, y);
        this.buffer.putFloat(this.attributeOffset + 8, z);
        this.nextAttribute();
        return this;
    }

    public BufferBuilder texture(float u, float v) {
        if (this.currentAttribute != VertexFormatAttribute.TEXTURE) return this;
        this.buffer.putFloat(this.attributeOffset, u);
        this.buffer.putFloat(this.attributeOffset + 4, v);
        this.nextAttribute();
        return this;
    }

    @Override
    public BufferBuilder colour(int red, int green, int blue, int alpha) {
        if (this.currentAttribute != VertexFormatAttribute.COLOR) return this;
        this.buffer.put(this.attributeOffset, (byte)(red));
        this.buffer.put(this.attributeOffset + 1, (byte)(green));
        this.buffer.put(this.attributeOffset + 2, (byte)(blue));
        this.buffer.put(this.attributeOffset + 3, (byte)(alpha));
        this.nextAttribute();
        return this;
    }

    public BuiltBuffer end() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }

        BuiltBuffer builtBuffer = new BuiltBuffer(this.format, this.mode, MemoryUtil.memSlice(this.buffer, 0, this.attributeOffset), this.vertexCount);
        this.format = null;
        this.mode = null;
        this.currentAttribute = null;
        this.currentAttributeId = 0;
        this.vertexCount = 0;
        this.building = false;
        return builtBuffer;
    }

    public class BuiltBuffer {
        private final VertexFormat format;
        private final DrawMode mode;
        private final ByteBuffer buffer;
        private final int vertexCount;

        private boolean released;

        public BuiltBuffer(VertexFormat format, DrawMode mode, ByteBuffer buffer, int vertexCount) {
            this.format = format;
            this.mode = mode;
            this.buffer = buffer;
            this.vertexCount = vertexCount;
        }

        public VertexFormat format() {
            return this.format;
        }

        public ByteBuffer buffer() {
            return this.buffer;
        }

        public DrawMode mode() {
            return this.mode;
        }

        public int vertexCount() {
            return this.vertexCount;
        }

        public int remaining() {
            return this.buffer.remaining();
        }

        public void release() {
            if (this.released) {
                return;
            }

            BufferBuilder.this.buffer.clear();
            this.released = true;
        }
    }
}
