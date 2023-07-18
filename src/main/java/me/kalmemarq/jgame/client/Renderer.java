package me.kalmemarq.jgame.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class Renderer {
    private static boolean textured2DCap = false;
    private static boolean blendCap = false;

    public static void clear(boolean color, boolean depth, boolean stencil) {
        int mask = 0;
        if (color) mask |= GL11.GL_COLOR_BUFFER_BIT;
        if (depth) mask |= GL11.GL_DEPTH_BUFFER_BIT;
        if (stencil) mask |= GL11.GL_STENCIL_BUFFER_BIT;

        if (mask != 0) {
            GL11.glClear(mask);
        }
    }

    public static void enableTexture() {
        if (textured2DCap) return;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        textured2DCap = true;
    }

    public static void disableTexture() {
        if (!textured2DCap) return;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        textured2DCap = false;
    }

    public static void enableBlend() {
        if (blendCap) return;
        GL11.glEnable(GL11.GL_BLEND);
        blendCap = true;
    }

    public static void disableBlend() {
        if (!blendCap) return;
        GL11.glDisable(GL11.GL_BLEND);
        blendCap = false;
    }

    public static void blendFunc(BlendFactor srcFactor, BlendFactor dstFactor) {
        GL11.glBlendFunc(srcFactor.glType, dstFactor.glType);
    }

    public static void blendSeparateFunc(BlendFactor srcRGBFactor, BlendFactor dstRGBFactor, BlendFactor srcAlphaFactor, BlendFactor dstAlphaFactor) {
        GL15.glBlendFuncSeparate(srcRGBFactor.glType, dstRGBFactor.glType, srcAlphaFactor.glType, dstAlphaFactor.glType);
    }

    @Deprecated
    public static void matrixMode(MatrixMode mode) {
        GL11.glMatrixMode(mode.glType);
    }

    @Deprecated
    public static void loadIdentity() {
        GL11.glLoadIdentity();
    }

    @Deprecated
    public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
        GL11.glOrtho(left, right, bottom, top, zNear, zFar);
    }

    @Deprecated
    public static void translate(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    @Deprecated
    public static void color(float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
    }

    @Deprecated
    public static void begin(PrimitiveType primitive) {
        GL11.glBegin(primitive.glType);
    }

    @Deprecated
    public static void vertex(float x, float y, float z) {
        GL11.glVertex3f(x, y, z);
    }

    @Deprecated
    public static void texCoord(float u, float v) {
        GL11.glTexCoord2f(u, v);
    }

    @Deprecated
    public static void end() {
        GL11.glEnd();
    }

    public enum BlendFactor {
        ZERO(GL11.GL_ZERO),
        ONE(GL11.GL_ONE),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        DST_COLOR(GL11.GL_DST_COLOR),
        DST_ALPHA(GL11.GL_DST_ALPHA),
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
        CONSTANT_COLOR(GL15.GL_CONSTANT_COLOR),
        CONSTANT_ALPHA(GL15.GL_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_COLOR(GL15.GL_ONE_MINUS_CONSTANT_COLOR),
        ONE_MINUS_CONSTANT_ALPHA(GL15.GL_ONE_MINUS_CONSTANT_ALPHA);

        public final int glType;

        BlendFactor(final int glType) {
            this.glType = glType;
        }
    }

    public enum PrimitiveType {
        TRIANGLES(GL11.GL_TRIANGLES),
        TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN),
        TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
        LINES(GL11.GL_LINES),
        LINE_STRIP(GL11.GL_LINE_STRIP),
        QUADS(GL11.GL_QUADS),
        GL_QUAD_STRIP(GL11.GL_QUAD_STRIP);

        public final int glType;

        PrimitiveType(final int glType) {
            this.glType = glType;
        }
    }

    @Deprecated
    public enum MatrixMode {
        PROJECTION(GL11.GL_PROJECTION),
        MODELVIEW(GL11.GL_MODELVIEW),
        TEXTURE(GL11.GL_TEXTURE);

        public final int glType;

        MatrixMode(final int glType) {
            this.glType = glType;
        }
    }
}