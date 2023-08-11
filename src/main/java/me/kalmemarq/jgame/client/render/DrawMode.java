package me.kalmemarq.jgame.client.render;

public enum DrawMode {
    QUADS(Renderer.PrimitiveType.TRIANGLES),
    TRIANGLES(Renderer.PrimitiveType.TRIANGLES);

    public final int glType;

    DrawMode(Renderer.PrimitiveType type) {
        this.glType = type.glType;
    }

    public int getIndexCount(int vertexCount) {
        return switch (this) {
            case TRIANGLES -> vertexCount;
            case QUADS -> vertexCount / 4 * 6;
        };
    }
}