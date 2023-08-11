package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.render.DrawContext;
import me.kalmemarq.jgame.client.render.Renderer;
import me.kalmemarq.jgame.client.sound.SoundInstance;
import org.lwjgl.glfw.GLFW;

public class TitleScreen extends Screen {
    private String[] items = {
        "Connect",
        "Options",
        "Quit"
    };
    
    private int selectedIdx;

    @Override
    public void render(DrawContext context) {
        Renderer.enableBlend();
        Renderer.defaultBlendFunc();
        
        context.drawTexture("assets/minicraft/textures/title.png", this.width / 2 - 56 - 20, 150, 0, 112, 16, 0, 0, 112, 16, 112, 16, 0xFF_FFFFFF);

        for (int i = 0; i < this.items.length; ++i) {
            context.drawString(this.items[i], this.width / 2 - this.client.font.textWidth(this.items[i]) / 2, 45 + (i * 10), 0xFF_FFFFFF);
        }

        Renderer.disableBlend();
    }

    @Override
    public void keyPressed(int key) {
        if (key == GLFW.GLFW_KEY_UP) {
            --this.selectedIdx;
            if (this.selectedIdx < 0) this.selectedIdx = 3;
            this.client.soundManager.play(new SoundInstance("/assets/minicraft/sounds/select.ogg", 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false));
        }

        if (key == GLFW.GLFW_KEY_DOWN) {
            ++this.selectedIdx;
            if (this.selectedIdx >= this.items.length) this.selectedIdx = 0;
            this.client.soundManager.play(new SoundInstance("/assets/minicraft/sounds/select.ogg", 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, false));
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
            if (this.selectedIdx == 0) {
                this.client.setScreen(new ConnectScreen());
            } else if (this.selectedIdx == 2) {
                this.client.scheduleShutdown();
            }
        }
    }
}
