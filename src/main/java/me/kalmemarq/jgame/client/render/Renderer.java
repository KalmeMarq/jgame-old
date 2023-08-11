package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.client.render.shader.Shader;
import me.kalmemarq.jgame.client.render.texture.Texture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLCapabilities;

import java.util.function.Supplier;

public class Renderer {
    public static boolean supportsGeometryShader;
    
    private static Matrix4f PROJECTION_MATRIX = new Matrix4f().identity();
    private static Matrix4f MODE_VIEW_MATRIX = new Matrix4f().identity();
    private static final float[] SHADER_COLOR = { 1.0f, 1.0f, 1.0f, 1.0f };
    private static Shader CURRENT_SHADER = null;
    private static final int[] SHADER_TEXTURES = new int[12];
    
    public static void initialize() {
        GLCapabilities capabilities = GL.getCapabilities();
        supportsGeometryShader = capabilities.GL_EXT_geometry_shader4 || capabilities.OpenGL32;
    }

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
    
    public static void setProjectionMatrixIdentity() {
        PROJECTION_MATRIX.identity();
    }

    public static Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public static void setModeViewMatrix(Matrix4f matrix) {
        MODE_VIEW_MATRIX = matrix;
    }

    public static void setModelViewMatrixIdentity() {
        MODE_VIEW_MATRIX.identity();
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

    public static void clear(boolean color, boolean depth, boolean stencil) {
        int mask = 0;
        if (color) mask |= GL11.GL_COLOR_BUFFER_BIT;
        if (depth) mask |= GL11.GL_DEPTH_BUFFER_BIT;
        if (stencil) mask |= GL11.GL_STENCIL_BUFFER_BIT;

        if (mask != 0) {
            GL11.glClear(mask);
        }
    }

    public static void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }
    
    public static void defaultBlendFunc() {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void enableDepthTest() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void disableDepthTest() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public static void enableAlphaTest() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    public static void disableAlphaTest() {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public static void enableScissorTest() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public static void disableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }
    
    public static void blendColor(float red, float green, float blue, float alpha) {
        GL15.glBlendColor(red, green, blue, alpha);
    }

    public static void blendEquation(BlendEquation mode) {
        GL14.glBlendEquation(mode.glType);
    }

    public static void blendEquationSeparate(BlendEquation modeRGB, BlendEquation modeAlpha) {
        GL20.glBlendEquationSeparate(modeRGB.glType, modeAlpha.glType);
    }
    
    public static void blendFunction(BlendFactor srcFactor, BlendFactor dstFactor) {
        GL11.glBlendFunc(srcFactor.glType, dstFactor.glType);
    }

    public static void blendFunctionSeparate(BlendFactor srcRGBFactor, BlendFactor dstRGBFactor, BlendFactor srcAlphaFactor, BlendFactor dstAlphaFactor) {
        GL15.glBlendFuncSeparate(srcRGBFactor.glType, dstRGBFactor.glType, srcAlphaFactor.glType, dstAlphaFactor.glType);
    }
    
    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL11.glColorMask(red, green, blue, alpha);
    }
    
    public static void depthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }
    
    public static void cullingMode(CullFace mode) {
        GL11.glCullFace(mode.glType);
    }

    public static void frontFace(FrontFace facing) {
        GL11.glFrontFace(facing.glType);
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
    
    public enum BlendEquation {
        FUNC_ADD(GL14.GL_FUNC_ADD),
        FUNC_SUBTRACT(GL14.GL_FUNC_SUBTRACT),
        FUNC_REVERSE_SUBTRACT(GL14.GL_FUNC_REVERSE_SUBTRACT),
        MIN(GL14.GL_MIN),
        MAX(GL14.GL_MAX);

        public final int glType;

        BlendEquation(final int glType) {
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
    
    public enum CullFace {
        FRONT(GL11.GL_FRONT),
        BACK(GL11.GL_BACK),
        FRONT_AND_BACK(GL11.GL_FRONT_AND_BACK);

        public final int glType;

        CullFace(final int glType) {
            this.glType = glType;
        }
    }
    
    public enum FrontFace {
        CW(GL11.GL_CW),
        CCW(GL11.GL_CCW);

        public final int glType;

        FrontFace(final int glType) {
            this.glType = glType;
        }
    }
}