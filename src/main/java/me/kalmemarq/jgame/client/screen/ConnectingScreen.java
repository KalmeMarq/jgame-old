package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.render.DrawContext;

public class ConnectingScreen extends Screen {
    public ConnectingScreen() {
    }

    @Override
    protected void init() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                this.client.connect();
            } catch (InterruptedException e) {
                this.client.setScreen(new DisconnectedScreen("COULD NOT CONNECT /2"));
            }
        }).start();
    }

    @Override
    public void render(DrawContext context) {
        context.drawString("CONNECTING...", this.client.window.getScaledWidth() / 2 - this.client.font.textWidth("CONNECTING...") / 2, this.client.window.getScaledHeight() / 2 - 4, 0xFF_FFFFFF);
    }
}
