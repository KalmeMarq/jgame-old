package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.Client;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.util.function.Supplier;

public class Renderer {
    private static Matrix4f PROJECTION_MATRIX = new Matrix4f().identity();
    private static Matrix4f MODE_VIEW_MATRIX = new Matrix4f().identity();
    private static final float[] SHADER_COLOR = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static Shader CURRENT_SHADER = null;
    private static final int[] SHADER_TEXTURES = new int[12];

    public static void setShaderTexture(int slot, int textureId) {
        SHADER_TEXTURES[slot] = textureId;
    }
    
    public static void setShaderTexture(int slot, String name) {
        Client client = Client.getInstance();
        Texture txr = client.textureManager.getTexture(name);
        SHADER_TEXTURES[slot] = txr.getId();
    }

    public static int getShaderTexture(int slot) {
        return SHADER_TEXTURES[slot];
    }

    public static void setCurrentShader(Supplier<Shader> currentShader) {
        CURRENT_SHADER = currentShader.get();
    }

    public static Shader getCurrentShader() {
        return CURRENT_SHADER;
    }

    public static void setProjectionMatrix(Matrix4f matrix) {
        PROJECTION_MATRIX = matrix;
    }

    public static Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public static void setModeViewMatrix(Matrix4f matrix) {
        MODE_VIEW_MATRIX = matrix;
    }

    public static Matrix4f getModeViewMatrix() {
        return MODE_VIEW_MATRIX;
    }
    
    public static void setShaderColor(float red, float green, float blue, float alpha) {
        SHADER_COLOR[0] = red;
        SHADER_COLOR[1] = green;
        SHADER_COLOR[2] = blue;
        SHADER_COLOR[3] = alpha;
    }

    public static float[] getShaderColor() {
        return SHADER_COLOR;
    }

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