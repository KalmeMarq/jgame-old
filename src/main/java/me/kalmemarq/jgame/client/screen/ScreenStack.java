package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.client.render.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ScreenStack {
    private final List<Screen> screens = new ArrayList<>();
    private final Client client;
    
    public ScreenStack(Client client) {
        this.client = client;
    }
    
    public void render(DrawContext context) {
        if (this.screens.isEmpty()) return;
        Screen topScreen = this.screens.get(this.screens.size() - 1);
        
        for (int i = 0; i < this.screens.size(); ++i) {
            if (i < this.screens.size() - 1) {
                if (topScreen.renderScreensBelow) this.screens.get(i).render(context);
            } else {
                this.screens.get(i).render(context);
            }
        }
    }
    
    public void resize(int width, int height) {
        for (Screen screen : this.screens) {
            screen.resize(width, height);
        }
    }
    
    public void keyPressed(int key) {
        if (this.screens.isEmpty()) return;
        this.screens.get(this.screens.size() - 1).keyPressed(key);
    }

    public void keyReleased(int key) {
        if (this.screens.isEmpty()) return;
        this.screens.get(this.screens.size() - 1).keyReleased(key);
    }

    public void charTyped(int codepoint) {
        if (this.screens.size() == 0) return;
        this.screens.get(this.screens.size() - 1).charTyped(codepoint);
    }
    
    public void push(Screen screen) {
        this.screens.add(screen);
        screen.init(this.client);
        screen.onDisplay();
    }
    
    public void pop() {
        Screen screen = this.screens.remove(this.screens.size() - 1);
        screen.onClose();
    }
    
    public void popAll() {
        for (int i = 0; i < this.screens.size(); ++i) {
            this.screens.get(i).onClose();
        }
        this.screens.clear();
    }
}
