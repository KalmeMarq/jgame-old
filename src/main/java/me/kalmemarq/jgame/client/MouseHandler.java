package me.kalmemarq.jgame.client;

import java.io.File;
import java.util.List;

public class MouseHandler implements Window.MouseEventHandler {
    private final Client client;
    private double x;
    private double y;

    public MouseHandler(Client client) {
        this.client = client;
    }
    
    @Override
    public void onMouseButton(int button, int action, int modifiers) {
    }

    @Override
    public void onCursorPos(double xpos, double ypos) {
        this.x = xpos;
        this.y = ypos;
    }

    @Override
    public void onScroll(double xoffset, double yoffset) {
    }

    @Override
    public void onFileDrop(List<File> files) {
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
}
