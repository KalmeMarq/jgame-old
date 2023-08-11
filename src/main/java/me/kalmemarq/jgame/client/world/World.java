package me.kalmemarq.jgame.client.world;

import me.kalmemarq.jgame.client.render.BufferBuilder;
import me.kalmemarq.jgame.client.render.Renderer;
import me.kalmemarq.jgame.client.render.shader.ShaderManager;
import me.kalmemarq.jgame.client.render.Tessellator;
import me.kalmemarq.jgame.client.render.VertexBuffer;
import me.kalmemarq.jgame.client.render.VertexFormat;
import me.kalmemarq.jgame.common.Destroyable;

public class World implements Destroyable {
    public int[] tiles;
    public boolean dirty = true;
    public VertexBuffer vertexBuffer = new VertexBuffer();
    
    public World() {
        this.tiles = new int[16 * 16];
        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                if (Math.random() > 0.7) {
                    this.tiles[y * 16 + x] = 0;
                } else {
                    this.tiles[y * 16 + x] = 1;
                }
            }
        }
    }
    
    public void render() {
        Renderer.setCurrentShader(ShaderManager::getPositionColorShader);
        Renderer.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        if (this.dirty) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBufferBuilder();

            builder.begin(VertexFormat.POSITION_COLOR);

            for (int y = 0; y < 16; ++y) {
                for (int x = 0; x < 16; ++x) {
                    int x0 = x * 16;
                    int y0 = y * 16;
                    int x1 = x0 + 16;
                    int y1 = y0 + 16;

                    int colour = this.tiles[y * 16 + x] == 0 ? 0xFF_FF0000 : 0xFF_0000FF; 

                    builder.vertex(x0, y0, 0).colour(colour).next();
                    builder.vertex(x0, y1, 0).colour(colour).next();
                    builder.vertex(x1, y1, 0).colour(colour).next();
                    builder.vertex(x1, y0, 0).colour(colour).next();
                }
            }

            this.dirty = false;
            this.vertexBuffer.bind();
            this.vertexBuffer.upload(builder.end());
            this.vertexBuffer.unbind();
        }

        this.vertexBuffer.bind();
        this.vertexBuffer.draw();
        this.vertexBuffer.unbind();
    }

    @Override
    public void destroy() {
        this.vertexBuffer.destroy();
    }
}
