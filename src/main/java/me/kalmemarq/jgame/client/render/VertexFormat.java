package me.kalmemarq.jgame.client.render;

import org.jetbrains.annotations.Nullable;

public class VertexFormat {
    public static final VertexFormat POSITION_COLOR = new VertexFormat(VertexFormatAttribute.POSITION, VertexFormatAttribute.COLOR);
    public static final VertexFormat POSITION_TEXTURE = new VertexFormat(VertexFormatAttribute.POSITION, VertexFormatAttribute.TEXTURE);
    public static final VertexFormat POSITION_TEXTURE_COLOR = new VertexFormat(VertexFormatAttribute.POSITION, VertexFormatAttribute.TEXTURE, VertexFormatAttribute.COLOR);

    public final VertexFormatAttribute[] attributes;
    private final int[] offsets;
    public final int stride;
    @Nullable
    private VertexBuffer vertexBuffer;

    public VertexFormat(VertexFormatAttribute... attributes) {
        if (attributes.length == 0) {
            throw new RuntimeException("Vertex format has no attributes");
        }

        this.attributes = attributes;
        this.offsets = new int[attributes.length];
        int srtd = 0;

        for (int i = 0; i < this.attributes.length; ++i) {
            this.offsets[i] = srtd;
            srtd += this.attributes[i].byteSize;
        }

        this.stride = srtd;
    }

    public VertexBuffer getVertexBuffer() {
        if (this.vertexBuffer == null) {
            this.vertexBuffer = new VertexBuffer();
        }
        return this.vertexBuffer;
    }

    public void setup() {
        for (int i = 0; i < this.attributes.length; ++i) {
            this.attributes[i].setup(i, this.stride, this.offsets[i]);
        }
    }

    public void clear() {
        for (int i = 0; i < this.attributes.length; ++i) {
            this.attributes[i].clear(i);
        }
    }
}
