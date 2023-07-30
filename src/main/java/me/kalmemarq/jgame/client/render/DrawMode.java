package me.kalmemarq.jgame.client.render;

import org.lwjgl.opengl.GL11;

public enum DrawMode {
    QUADS(GL11.GL_TRIANGLES),
    TRIANGLES(GL11.GL_TRIANGLES);

    public final int glType;

    DrawMode(int glType) {
        this.glType = glType;
    }

    public int getIndexCount(int vertexCount) {
        return switch (this) {
            case TRIANGLES -> vertexCount;
            case QUADS -> vertexCount / 4 * 6;
        };
    }
}