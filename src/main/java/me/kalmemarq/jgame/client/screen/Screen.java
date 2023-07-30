package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.client.render.DrawContext;

public class Screen {
    protected Client client;
    protected int width;
    protected int height;

    public void init(Client client) {
        this.client = client;
        this.width = client.window.getScaledWidth();
        this.height = client.window.getScaledHeight();
        this.init();
    }

    protected void init() {
    }

    public void onDisplay() {
    }

    public void onClose() {
    }

    public void render(DrawContext context) {
    }

    public void tick() {
    }

    public void mousePressed(int mouseX, int mouseY, int button) {
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void keyPressed(int key) {
    }

    public void keyReleased(int key) {
    }

    public void charTyped(int codepoint) {
    }
}
