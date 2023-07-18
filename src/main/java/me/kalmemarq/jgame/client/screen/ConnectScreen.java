package me.kalmemarq.jgame.client.screen;

import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class ConnectScreen extends Screen {
    private int selectedIdx = 0;
    private String ip;
    private String username;

    @Override
    protected void init() {
        this.ip = "LOCALHOST:8080";
        this.username = "PLAYER" + (int)(Math.random() * 2000);
    }

    @Override
    public void render() {
        this.client.font.drawText("IP: " + this.ip, 10, 10, this.selectedIdx == 0 ? 0xFF_FFFFFF : 0xFF_999999);
        this.client.font.drawText("USERNAME: " + this.username, 10, 20, this.selectedIdx == 1 ? 0xFF_FFFFFF : 0xFF_999999);

        this.client.font.drawText("CONNECT", 10, 40, this.selectedIdx == 2 ? 0xFF_FFFFFF : 0xFF_999999);
        this.client.font.drawText("QUIT", 10, 50, this.selectedIdx == 3 ? 0xFF_FFFFFF : 0xFF_999999);
    }

    @Override
    public void keyPressed(int key) {
        if (key == GLFW.GLFW_KEY_UP) {
            --this.selectedIdx;
            if (this.selectedIdx < 0) this.selectedIdx = 3;
        }

        if (key == GLFW.GLFW_KEY_DOWN) {
            ++this.selectedIdx;
            if (this.selectedIdx >= 4) this.selectedIdx = 0;
        }

        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_SPACE) {
            if (this.selectedIdx == 2) {
                this.client.setScreen(new ConnectingScreen());
            } else if (this.selectedIdx == 3) {
                this.client.scheduleShutdown();
            }
        }

        if (key == GLFW.GLFW_KEY_BACKSPACE && GLFW.glfwGetKey(this.client.window.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS) {
            if (this.selectedIdx == 0) this.ip = "";
            if (this.selectedIdx == 1) this.username = "";
        } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.selectedIdx == 0) {
                if (this.ip.length() > 0) {
                    this.ip = this.ip.substring(0, this.ip.length() - 1);
                }
            }
            if (this.selectedIdx == 1) {
                if (this.username.length() > 0) {
                    this.username = this.username.substring(0, this.username.length() - 1);
                }
            }
        }
    }

    @Override
    public void charTyped(int codepoint) {
        if (this.selectedIdx == 0) {
            this.ip += Character.toString(codepoint).toUpperCase(Locale.ROOT);
        } else if (this.selectedIdx == 1) {
            this.username += Character.toString(codepoint).toUpperCase(Locale.ROOT);
        }
    }
}
