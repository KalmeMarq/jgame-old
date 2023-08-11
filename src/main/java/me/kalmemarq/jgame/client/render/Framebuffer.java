package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.render.shader.ShaderManager;
import me.kalmemarq.jgame.common.Destroyable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Framebuffer implements Destroyable {
    private boolean initialized;
    private int fbo = -1;
    private int ftexture = -1;
    private int rbo = -1;
    private int width;
    private int height;

    public Framebuffer() {
    }
    
    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void resize(int width, int height) {
        this.initialized = true;
        this.width = width;
        this.height = height;
        
        if (this.fbo != -1) {
            GL30.glDeleteRenderbuffers(this.rbo);
        }

        if (this.ftexture != -1) {
            GL11.glDeleteTextures(this.ftexture);
        }

        if (this.fbo != -1) {
            GL30.glDeleteFramebuffers(this.fbo);
        }
        
        this.fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);

        this.ftexture = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.ftexture);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, this.width, this.height, 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, 0L);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, this.ftexture, 0);

        this.rbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.rbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, this.width, this.height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.rbo);

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer Error: " + getBetterError(status));
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
    
    private static String getBetterError(int error) {
        return switch (error) {
            case GL30.GL_FRAMEBUFFER_UNDEFINED -> "Undefined";
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED -> "Unsupported";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "Incomplete attachment";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "Missing attachment";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "Incomplete read buffer";
            default -> "Unknown error " + error;
        };
    }
    
    public void begin() {
        if (!this.initialized) {
            this.resize(this.width, this.height);
        }
        
        GL11.glViewport(0, 0, this.width, this.height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
    }
    
    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);    
    }
    
    public void draw() {
        GL11.glColorMask(true, true, true, false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        
        Renderer.setCurrentShader(ShaderManager::getBlitShader);
        Renderer.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Renderer.setShaderTexture(0, this.ftexture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        builder.begin(VertexFormat.POSITION_TEXTURE);
        builder.vertex(-1, -1, 0).texture(0, 0).next();
        builder.vertex(-1, 1, 0).texture(0, 1).next();
        builder.vertex(1, 1, 0).texture(1, 1).next();
        builder.vertex(1, -1, 0).texture(1, 0).next();
        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glColorMask(true, true, true, true);
    }

    @Override
    public void destroy() {
        GL30.glDeleteRenderbuffers(this.rbo);
        GL11.glDeleteTextures(this.ftexture);
        GL30.glDeleteFramebuffers(this.fbo);
    }
}
