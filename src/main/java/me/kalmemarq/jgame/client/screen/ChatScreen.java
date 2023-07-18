package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.Renderer;
import me.kalmemarq.jgame.common.packet.CommandC2SPacket;
import me.kalmemarq.jgame.common.packet.MessagePacket;
import me.kalmemarq.jgame.common.packet.PingPacket;
import org.lwjgl.glfw.GLFW;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public class ChatScreen extends Screen {
    private String input = "";

    @Override
    public void render() {
        int y = this.client.window.getScaledHeight() - 20 - 2;
        for (int i = this.client.messages.size() - 1; i >= 0; --i) {
            List<String> l = this.client.font.breakTextIntoLines(this.client.messages.get(i), this.client.window.getScaledWidth() - 10);

            for (int j = l.size() - 1; j >= 0; --j) {
                this.client.font.drawText(l.get(j), 1, y, 0xFFEEEEEE);

                y -= 10;
            }
        }

        Renderer.disableTexture();
        Renderer.begin(Renderer.PrimitiveType.QUADS);
        Renderer.color(1.0f, 1.0f, 1.0f, 1.0f);
        Renderer.vertex(0, this.client.window.getScaledHeight() - 13, 0);
        Renderer.vertex(0, this.client.window.getScaledHeight() - 12, 0);
        Renderer.vertex(this.client.window.getScaledWidth(), this.client.window.getScaledHeight() - 12, 0);
        Renderer.vertex(this.client.window.getScaledWidth(), this.client.window.getScaledHeight() - 13, 0);
        Renderer.end();
        Renderer.enableTexture();

        this.client.font.drawText(">" + this.input, 1, this.client.window.getScaledHeight() - 10, 0xFF_FFFFFF);
    }

    @Override
    public void keyPressed(int key) {
        if (key == GLFW.GLFW_KEY_BACKSPACE && GLFW.glfwGetKey(this.client.window.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ) {
            this.input = "";
        } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.input.length() > 0) {
                this.input = this.input.substring(0, this.input.length() - 1);
            }
        }
    }

    @Override
    public void keyReleased(int key) {
        if (key == GLFW.GLFW_KEY_ENTER) {
            if (this.input.startsWith("/PING")) {
                this.client.connection.sendPacket(new PingPacket(System.nanoTime()));
                this.input = "";
            } else if (this.input.startsWith("/QUIT")) {
                this.client.connection.disconnect();
                this.client.setScreen(new ConnectScreen());
                this.input = "";
            } else if (this.input.startsWith("/")) {
                this.client.connection.sendPacket(new CommandC2SPacket(this.input.substring(1)));
                this.input = "";
            } else {
                this.client.connection.sendPacket(new MessagePacket("KalmeMarq", Instant.now(), this.input));
                this.input = "";
            }
        }
    }

    @Override
    public void charTyped(int codepoint) {
        this.input += Character.toString(codepoint).toUpperCase(Locale.ROOT);
    }
}
