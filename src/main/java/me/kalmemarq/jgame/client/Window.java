package me.kalmemarq.jgame.client;

import me.kalmemarq.jgame.client.render.Renderer;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.OperatingSystem;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class Window implements Destroyable {
    private long handle;
    private String title;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int scaledWidth;
    private int scaledHeight;
    private boolean vsync;
    private WindowEventHandler windowEventHandler;
    private boolean focused;

    public Window(int width, int height, String title, boolean vsync) {
        this.title = title;
        this.width = Math.max(width, 1);
        this.height = Math.max(height, 1);
        this.vsync = vsync;
    }

    public void init(WindowEventHandler windowHandler, MouseEventHandler mouseHandler, KeyboardEventHandler keyboardHandler) throws RuntimeException {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        this.windowEventHandler = windowHandler;

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        if (OperatingSystem.get() == OperatingSystem.MACOS) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        }

        this.handle = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0L, 0L);

        if (this.handle == 0L) {
            this.destroy();
            throw new RuntimeException("Failed to create GLFW window!");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pW = stack.mallocInt(1);
            IntBuffer pH = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(this.handle, pW, pH);

            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
      
            if (vidMode != null) GLFW.glfwSetWindowPos(this.handle, (vidMode.width() - pW.get(0)) / 2, (vidMode.height() - pH.get(0)) / 2);
        }

        GLFW.glfwMakeContextCurrent(this.handle);
        GLFW.glfwSwapInterval(this.vsync ? 1 : 0);
        GLFW.glfwShowWindow(this.handle);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pX = stack.mallocInt(1);
            IntBuffer pY = stack.mallocInt(1);

            GLFW.glfwGetWindowPos(this.handle, pX, pY);

            this.x = pX.get(0);
            this.y = pY.get(0);

            IntBuffer pW = stack.mallocInt(1);
            IntBuffer pH = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(this.handle, pW, pH);
            this.width = pW.get(0);
            this.height = pH.get(0);
            IntBuffer pFW = stack.mallocInt(1);
            IntBuffer pFH = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(this.handle, pFW, pFH);
            this.framebufferWidth = pFW.get(0);
            this.framebufferHeight = pFH.get(0);
            this.scaledWidth = (int) (this.framebufferWidth / 3d);
            this.scaledHeight = (int) (this.framebufferHeight / 3d);
        }

        GLFW.glfwSetWindowPosCallback(this.handle, this::onWindowPosChanged);
        GLFW.glfwSetWindowSizeCallback(this.handle, this::onWindowSizeChanged);
        GLFW.glfwSetFramebufferSizeCallback(this.handle, this::onFramebufferSizeChanged);
        GLFW.glfwSetWindowFocusCallback(this.handle, this::onWindowFocusChanged);

        GLFW.glfwSetMouseButtonCallback(this.handle, (_w, button, action, mods) -> mouseHandler.onMouseButton(button, action, mods));
        GLFW.glfwSetCursorPosCallback(this.handle, (_w, xpos, ypos) -> mouseHandler.onCursorPos(xpos, ypos));
        GLFW.glfwSetScrollCallback(this.handle, (_w, xoffset, yoffset) -> mouseHandler.onScroll(xoffset, yoffset));
        GLFW.glfwSetDropCallback(this.handle, (_w, count, names) -> {
            List<File> files = new ArrayList<>();
            for (int i = 0; i < count; ++i) files.add(new File(GLFWDropCallback.getName(names, i)));
            mouseHandler.onFileDrop(files);
        });
        GLFW.glfwSetKeyCallback(this.handle, (_w, key, scancode, action, mods) -> keyboardHandler.onKey(key, scancode, action, mods));
        GLFW.glfwSetCharCallback(this.handle, (_w, codepoint) -> keyboardHandler.onCharTyped(codepoint));

        try {
            this.setIcon(
                Window.class.getResourceAsStream("/assets/minicraft/icons/icon16.png"),
                Window.class.getResourceAsStream("/assets/minicraft/icons/icon32.png"),
                Window.class.getResourceAsStream("/assets/minicraft/icons/icon48.png"),
                Window.class.getResourceAsStream("/assets/minicraft/icons/icon64.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        GL.createCapabilities();
        Renderer.initialize();
        GL11.glViewport(0, 0, this.width, this.height);
        this.focused = GLFW.glfwGetWindowAttrib(this.handle, GLFW.GLFW_FOCUSED) == 1;
    }
    
    public void setIcon(InputStream ...iconStreams) {
        int platform = GLFW.glfwGetPlatform();

        if (platform == GLFW.GLFW_PLATFORM_WIN32 || platform == GLFW.GLFW_PLATFORM_X11) {
            int[] widths = new int[16];
            int[] heights = new int[16];
            List<ByteBuffer> buf0 = new ArrayList<>();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                for (int i = 0, j = 0; i < iconStreams.length; ++i) {
                    ByteBuffer buffer;
                    try {
                        buffer = MemoryUtils.readAsByteBuffer(iconStreams[i]);
                    } catch (IOException e) {
                        continue;
                    }
                    buffer.rewind();

                    IntBuffer pWidth = stack.mallocInt(1);
                    IntBuffer pHeight = stack.mallocInt(1);
                    IntBuffer pComps = stack.mallocInt(1);

                    ByteBuffer buffer1 = STBImage.stbi_load_from_memory(buffer, pWidth, pHeight, pComps, 4);

                    if (buffer1 != null) {
                        widths[j] = pWidth.get(0);
                        heights[j] = pHeight.get(0);
                        buf0.add(buffer1);
                    }

                    MemoryUtil.memFree(buffer);
                    ++j;
                }
            }

            try (GLFWImage.Buffer icons = GLFWImage.malloc(buf0.size())) {
                for (int i = 0; i < buf0.size(); ++i) {
                    icons.position(i).width(widths[i]).height(heights[i]).pixels(buf0.get(i));
                }
                GLFW.glfwSetWindowIcon(this.handle, icons);
            }

            for (ByteBuffer b : buf0) {
                STBImage.stbi_image_free(b);
            }
        } else if (platform == GLFW.GLFW_PLATFORM_COCOA) {
            // TODO: https://github.com/shannah/Java-Objective-C-Bridge
        }
    }

    public boolean isFocused() {
        return this.focused;
    }
    
    public void setVsync(boolean vsync) {
        if (this.vsync == vsync) return;
        this.vsync = vsync;
        GLFW.glfwSwapInterval(this.vsync ? 1 : 0);
    }

    public boolean isVsyncEnabled() {
        return this.vsync;
    }

    public void setTitle(String title) {
        this.title = title;
        GLFW.glfwSetWindowTitle(this.handle, this.title);
    }

    private void onWindowFocusChanged(long window, boolean focused) {
        this.focused = focused;
        this.windowEventHandler.onFocusChanged();
    }

    private void onWindowPosChanged(long window, int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void onWindowSizeChanged(long window, int width, int height) {
        this.width = width;
        this.height = height;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pX = stack.mallocInt(1);
            IntBuffer pY = stack.mallocInt(1);

            GLFW.glfwGetWindowPos(this.handle, pX, pY);

            this.x = pX.get(0);
            this.y = pY.get(0);
        }
    }

    private void onFramebufferSizeChanged(long window, int width, int height) {
        this.framebufferWidth = width;
        this.framebufferHeight = height;
        this.scaledWidth = (int) Math.ceil(width / 3d);
        this.scaledHeight = (int) Math.ceil(height / 3d);
        this.windowEventHandler.onSizeChanged();
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

    public int getFramebufferWidth() {
        return this.framebufferWidth;
    }

    public int getFramebufferHeight() {
        return this.framebufferHeight;
    }

    public int getScaledWidth() {
        return this.scaledWidth;
    }

    public int getScaledHeight() {
        return this.scaledHeight;
    }

    public interface WindowEventHandler {
        void onSizeChanged();
        void onFocusChanged();
    }

    public interface MouseEventHandler {
        void onMouseButton(int button, int action, int modifiers);
        void onCursorPos(double xpos, double ypos);
        void onScroll(double xoffset, double yoffset);
        void onFileDrop(List<File> files);
    }

    public interface KeyboardEventHandler {
        void onKey(int key, int scancode, int action, int modifiers);
        void onCharTyped(int codepoint);
    }
}
