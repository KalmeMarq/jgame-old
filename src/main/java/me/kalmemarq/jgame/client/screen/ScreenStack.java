package me.kalmemarq.jgame.client.screen;

import me.kalmemarq.jgame.client.render.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ScreenStack {
    private List<Screen> screens = new ArrayList<>();
    
    public void render(DrawContext context) {
        for (int i = 0; i < this.screens.size(); ++i) {
            this.screens.get(i).render(context);
        }
    }
    
    public void push(Screen screen) {
        this.screens.add(screen);
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
