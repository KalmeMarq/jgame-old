package me.kalmemarq.jgame.client;

import org.lwjgl.glfw.GLFW;

public class KeyboardHandler implements Window.KeyboardEventHandler {
    private final Client client;
    
    public KeyboardHandler(Client client) {
        this.client = client;
    }
    
    @Override
    public void onKey(int key, int scancode, int action, int modifiers) {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F2) {
            this.client.takeScreenshot();
        }

        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F3) {
            this.client.imGuiLayer.toggleVisibility();
        }

        if (action != GLFW.GLFW_RELEASE) {
            this.client.screenStack.keyPressed(key);
        } else {
            this.client.screenStack.keyReleased(key);
        }
    }

    @Override
    public void onCharTyped(int codepoint) {
        this.client.screenStack.charTyped(codepoint);
    }
}
