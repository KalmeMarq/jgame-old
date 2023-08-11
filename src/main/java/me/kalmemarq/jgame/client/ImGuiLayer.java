package me.kalmemarq.jgame.client;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import me.kalmemarq.jgame.client.render.texture.Texture;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.logger.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ImGuiLayer implements Destroyable {
    private static final Logger LOGGER = Logger.getLogger();

    private final Client client;
    private boolean initialized;
    private ImGuiImplGl3 imGuiGl3;
    private ImGuiImplGlfw imGuiGlfw;
    private boolean show;
    private ImBoolean showWindow;
    private ImBoolean showTextures;

    public ImGuiLayer(Client client) {
        this.client = client;
    }

    public void init() {
        if (this.initialized) {
            throw new RuntimeException("Already initialized!");
        }
        this.initialized = true;
        this.imGuiGl3 = new ImGuiImplGl3();
        this.imGuiGlfw = new ImGuiImplGlfw();
        ImGui.createContext();
        ImGui.getIO().setIniFilename(null);
        this.imGuiGlfw.init(this.client.window.getHandle(), false);
        this.imGuiGl3.init("#version 330");
        this.showWindow = new ImBoolean();
        this.showTextures = new ImBoolean();
        LOGGER.debug("ImGui initialized!");
    }

    public void toggleVisibility() {
        this.show = !this.show;
    }

    public void render() {
        if (!this.show || !this.initialized) return;
        this.imGuiGlfw.newFrame();
        ImGui.newFrame();

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.menuItem("Window")) {
                this.showWindow.set(true);
            }

            if (ImGui.menuItem("Texture Manager")) {
                this.showTextures.set(true);
            }

            if (ImGui.menuItem("Exit")) {
                Client.getInstance().scheduleShutdown();
            }
        }
        ImGui.endMainMenuBar();

        if (this.showWindow.get()) {
            if (ImGui.begin("Window", this.showWindow)) {
                ImGui.text("Window Position: " + this.client.window.getX() + " " + this.client.window.getY());
                ImGui.text("Window Size: " + this.client.window.getWidth() + "x" + this.client.window.getHeight());
                ImGui.text("Window Framebuffer Size: " + this.client.window.getFramebufferWidth() + "x" + this.client.window.getFramebufferHeight());
                ImGui.text("Window Focused: " + (this.client.window.isFocused() ? "true" : "false"));
                ImGui.text("Vsync: " + (this.client.window.isVsyncEnabled() ? "true" : "false"));
            }
            ImGui.end();
        }

        if (this.showTextures.get()) {
            if (ImGui.begin("Texture Manager", this.showTextures)) {
                for (Map.Entry<String, Texture> entry : this.client.textureManager.getTextures()) {
                    Texture txr = entry.getValue();
                    ImGui.image(txr.getId(), txr.getWidth(), txr.getHeight());
                    if (ImGui.isItemHovered()) {
                        ImGui.setTooltip(entry.getKey());
                    }
                }
            }
            ImGui.end();
        }

        ImGui.render();
        this.imGuiGl3.renderDrawData(ImGui.getDrawData());
        long backupWindowPtr = GLFW.glfwGetCurrentContext();
        ImGui.updatePlatformWindows();
        ImGui.renderPlatformWindowsDefault();
        GLFW.glfwMakeContextCurrent(backupWindowPtr);
    }

    @Override
    public void destroy() {
        GLFW.glfwSetMonitorCallback(null).free();
        this.imGuiGl3.dispose();
        ImGui.destroyContext();
    }
}
