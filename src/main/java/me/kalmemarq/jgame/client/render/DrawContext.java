package me.kalmemarq.jgame.client.render;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.client.render.shader.ShaderManager;

public class DrawContext {
    private final Client client;
    private final MatrixStack matrices;
    
    public DrawContext(Client client) {
        this.client = client;
        this.matrices = new MatrixStack();
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public void drawString(String text, int x, int y, int colour) {
        this.client.font.drawString(text, x, y, colour);
    }

    public void drawStringM(String text, int x, int y, int colour) {
        this.client.font.drawString(this.matrices.peek(), text, x, y, colour);
    }
    
    public void drawTexture(String name, int x, int y, int z, int width, int height, int u, int v, int us, int vs, int textureWidth, int textureHeight, int tint) {
        int r = (tint >> 16) & 0xFF;
        int g = (tint >> 8) & 0xFF;
        int b = tint & 0xFF;
        int a = (tint >> 24) & 0xFF;

        float x1 = x + width;
        float y1 = y + height;
      
        float u0 = u / (float) textureWidth;
        float v0 = v / (float) textureHeight;
        float u1 = (u + us) / (float) textureWidth;
        float v1 = (v + vs) / (float) textureHeight;
        
        Renderer.setCurrentShader(ShaderManager::getPositionTextureColorShader);
        Renderer.setShaderTexture(0, name);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        builder.begin(VertexFormat.POSITION_TEXTURE_COLOR);
        builder.vertex(x, y, z).texture(u0, v0).colour(r, g, b, a).next();
        builder.vertex(x, y1, z).texture(u0, v1).colour(r, g, b, a).next();
        builder.vertex(x1, y1, z).texture(u1, v1).colour(r, g, b, a).next();
        builder.vertex(x1, y, z).texture(u1, v0).colour(r, g, b, a).next();
        tessellator.draw();
    }
    
    public void drawColoured(int x, int y, int z, int width, int height, int colour) {
        int r = (colour >> 16) & 0xFF;
        int g = (colour >> 8) & 0xFF;
        int b = colour & 0xFF;
        int a = (colour >> 24) & 0xFF;
        
        float x1 = x + width;
        float y1 = y + height;
        
        Renderer.setCurrentShader(ShaderManager::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        builder.begin(VertexFormat.POSITION_COLOR);
        builder.vertex(x, y, z).colour(r, g, b, a).next();
        builder.vertex(x, y1, z).colour(r, g, b, a).next();
        builder.vertex(x1, y1, z).colour(r, g, b, a).next();
        builder.vertex(x1, y, z).colour(r, g, b, a).next();
        tessellator.draw();
    }

    public void drawVGradient(int x, int y, int z, int width, int height, int colourStart, int colourEnd) {
        int sr = (colourStart >> 16) & 0xFF;
        int sg = (colourStart >> 8) & 0xFF;
        int sb = colourStart & 0xFF;
        int sa = (colourStart >> 24) & 0xFF;
        int er = (colourEnd >> 16) & 0xFF;
        int eg = (colourEnd >> 8) & 0xFF;
        int eb = colourEnd & 0xFF;
        int ea = (colourEnd >> 24) & 0xFF;

        int x1 = x + width;
        int y1 = y + height;

        Renderer.setCurrentShader(ShaderManager::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        builder.begin(VertexFormat.POSITION_COLOR);
        builder.vertex(x, y, z).colour(sr, sg, sb, sa).next();
        builder.vertex(x, y1, z).colour(er, eg, eb, ea).next();
        builder.vertex(x1, y1, z).colour(er, eg, eb, ea).next();
        builder.vertex(x1, y, z).colour(sr, sg, sb, sa).next();
        tessellator.draw();
    }

    public void drawHGradient(int x, int y, int z, int width, int height, int colourStart, int colourEnd) {
        int sr = (colourStart >> 16) & 0xFF;
        int sg = (colourStart >> 8) & 0xFF;
        int sb = colourStart & 0xFF;
        int sa = (colourStart >> 24) & 0xFF;
        int er = (colourEnd >> 16) & 0xFF;
        int eg = (colourEnd >> 8) & 0xFF;
        int eb = colourEnd & 0xFF;
        int ea = (colourEnd >> 24) & 0xFF;

        int x1 = x + width;
        int y1 = y + height;

        Renderer.setCurrentShader(ShaderManager::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        builder.begin(VertexFormat.POSITION_COLOR);
        builder.vertex(x, y, z).colour(sr, sg, sb, sa).next();
        builder.vertex(x, y1, z).colour(sr, sg, sb, sa).next();
        builder.vertex(x1, y1, z).colour(er, eg, eb, ea).next();
        builder.vertex(x1, y, z).colour(er, eg, eb, ea).next();
        tessellator.draw();
    }
}
