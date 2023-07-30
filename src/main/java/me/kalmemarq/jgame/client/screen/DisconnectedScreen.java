package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.render.DrawContext;
import org.lwjgl.glfw.GLFW;

public class DisconnectedScreen extends Screen {
    private final String reason;

    public DisconnectedScreen(String reason) {
        this.reason = reason;
    }

    @Override
    public void render(DrawContext context) {
        context.drawString(this.reason, this.client.window.getScaledWidth() / 2 - this.client.font.textWidth(this.reason) / 2, this.client.window.getScaledHeight() / 2 - 4, 0xFF_FFFFFF);
    }

    @Override
    public void keyPressed(int key) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
            this.client.setScreen(new ConnectScreen());
        }
    }
}
