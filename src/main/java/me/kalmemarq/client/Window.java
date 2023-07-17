package me.kalmemarq.client;

import me.kalmemarq.common.Destroyable;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.Closeable;

public class Window implements Destroyable {
    private long handle;
    private String title;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean vsync;
    private boolean fullscreen;

    public Window(int width, int height, String title) {
        this.title = title;
        this.width = Math.max(width, 1);
        this.height = Math.max(height, 1);
    }

    public void init() throws RuntimeException {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        this.handle = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0L, 0L);

        if (this.handle == 0L) {
            this.destroy();
            throw new RuntimeException("Failed to create GLFW window!");
        }

        GLFW.glfwSwapInterval(1);
        GLFW.glfwMakeContextCurrent(this.handle);
        GLFW.glfwShowWindow(this.handle);

        GL.createCapabilities();
        GL11.glViewport(0, 0, this.width, this.height);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(this.handle);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(this.handle);
        GLFW.glfwPollEvents();
    }

    @Override
    public void destroy() {
        if (this.handle != 0L) {
            Callbacks.glfwFreeCallbacks(this.handle);
            GLFW.glfwDestroyWindow(this.handle);
        }

        GLFW.glfwTerminate();
    }

    public long getHandle() {
        return this.handle;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
