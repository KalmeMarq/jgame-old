package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Framebuffer implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();
    private int fbo = -1;
    private int ftexture = -1;
    private int rbo = -1;
    private int width;
    private int height;

    public Framebuffer() {
    }
    
    private void resize(Client client) {
        if (this.fbo != -1) {
            GL30.glDeleteRenderbuffers(this.rbo);
        }

        if (this.ftexture != -1) {
            GL30.glDeleteTextures(this.ftexture);
        }

        if (this.fbo != -1) {
            GL30.glDeleteFramebuffers(this.fbo);
        }
        
        this.fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);

        this.ftexture = GL11.glGenTextures();
        GL30.glBindTexture(GL11.GL_TEXTURE_2D, this.ftexture);
        GL30.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, this.width, this.height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0L);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, this.ftexture, 0);

        this.rbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, this.rbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, this.width, this.height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, this.rbo);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
    
    public void begin() {
        Client client = Client.getInstance();
        if (this.width != client.window.getFramebufferWidth() || this.height != client.window.getFramebufferHeight()) {
            this.width = client.window.getFramebufferWidth();
            this.height = client.window.getFramebufferHeight();
            LOGGER.debug("Resizing framebuffer");
            this.resize(client);
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
    }
    
    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);    
    }
    
    public void draw() {
        GL30.glColorMask(true, true, true, false);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glDepthMask(false);
        
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

        GL30.glDepthMask(true);
        GL30.glColorMask(true, true, true, true);
    }

    @Override
    public void destroy() {
        GL30.glDeleteRenderbuffers(this.rbo);
        GL11.glDeleteTextures(this.ftexture);
        GL30.glDeleteFramebuffers(this.fbo);
    }
}
