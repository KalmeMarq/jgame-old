package me.kalmemarq.jgame.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.kalmemarq.jgame.client.network.ClientNetworkHandler;
import me.kalmemarq.jgame.client.render.*;
import me.kalmemarq.jgame.client.resource.ResourceLoader;
import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.client.screen.ChatScreen;
import me.kalmemarq.jgame.client.screen.ConnectScreen;
import me.kalmemarq.jgame.client.screen.DisconnectedScreen;
import me.kalmemarq.jgame.client.screen.Screen;
import me.kalmemarq.jgame.client.sound.SoundManager;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.ThreadExecutor;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Client extends ThreadExecutor implements Destroyable, Window.WindowEventHandler, Window.MouseEventHandler, Window.KeyboardEventHandler {
    private static Client INSTANCE;
    
    private final Thread clientThread;
    public final Window window;
    public final Font font;
    public final SoundManager soundManager;
    public final TextureManager textureManager;
    private boolean running;
    public GameOptions options;
    public NetworkConnection connection;
    public List<String> messages = new ArrayList<>();
    private Screen screen;
    private final ResourceLoader resourceLoader;
    private final ShaderManager shaderManager;
    private final Framebuffer framebuffer;
    private final ResourceManager resourceManager = new ResourceManager();

    public Client(File gameDir) {
        INSTANCE = this;
        this.clientThread = Thread.currentThread();
        this.options = new GameOptions(gameDir);
        this.options.load();
        this.window = new Window(800, 600, "JGame", this.options.vsync.getValue());
        this.soundManager = new SoundManager();
        this.textureManager = new TextureManager(this.resourceManager);
        this.font = new Font();
        this.resourceLoader = new ResourceLoader();
        this.shaderManager = new ShaderManager();
        this.framebuffer = new Framebuffer();
    }

    public void run() {
        this.window.init(this, this, this);
        this.soundManager.init();
        this.running = true;

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        this.resourceLoader.start(List.of(this.shaderManager, this.textureManager, this.font), Util.RELOADER_WORKER, this, this.resourceManager, () -> {
        });
        
        try {
            long lastTime = System.currentTimeMillis();
            long lastTimeTick = System.nanoTime();
            int frameCounter = 0;
            int tickCounter = 0;
            double unprocessed = 0;

            this.setScreen(new ConnectScreen());
            while (this.running) {
                if (this.window.shouldClose()) this.running = false;

                this.runQueueTask();

                long now = System.nanoTime();
                double nsPerTick = 1E9D / 20;
                unprocessed += (now - lastTimeTick) / nsPerTick;
                lastTimeTick = now;
                while (unprocessed >= 1) {
                    tickCounter++;
                    this.tick();
                    unprocessed--;
                }

                this.framebuffer.begin();
                Renderer.clear(true, true, false);
                
                this.render();

                this.framebuffer.end();
                
                Renderer.clear(true, false, false);
                Renderer.setProjectionMatrix(new Matrix4f().identity());
                Renderer.setModeViewMatrix(new Matrix4f().identity());

                this.framebuffer.draw();
                
                this.window.swapBuffers();

                ++frameCounter;

                while (System.currentTimeMillis() - lastTime > 1000L) {
                    this.window.setTitle("Client " + frameCounter + " FPS " + tickCounter + " TPS");
                    frameCounter = 0;
                    tickCounter = 0;
                    lastTime += 1000L;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.running = false;
        }
     
        this.options.save();
        this.soundManager.destroy();
        this.framebuffer.destroy();
    }

    private void tick() {
        if (this.screen != null) this.screen.tick();
    }
    
    private void render() {
        Renderer.setProjectionMatrix(Renderer.getProjectionMatrix().identity().setOrtho(0, this.window.getFramebufferWidth() / 2f, this.window.getFramebufferHeight() / 2f, 0, 1000, 3000));
        Renderer.setModeViewMatrix(Renderer.getModeViewMatrix().identity().translate(0, 0, -2000.0f));
        Renderer.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        DrawContext context = new DrawContext(this);
        
        if (this.resourceLoader.isActive()) {
            if (ShaderManager.hasLoadedInitial()) {
                context.drawColoured(0, this.window.getScaledHeight() - 23, 0, (int) (this.window.getScaledWidth() * this.resourceLoader.getProgress()), 12, 0xFF_FFFFFF);
                context.drawString("PROGRESS: " + (int)(this.resourceLoader.getProgress() * 100) + "%", 1, this.window.getScaledHeight() - 40, 0xFF_FFFFFF);
            }
        } else {
            if (this.screen != null) this.screen.render(context);
        }
    }

    public void setScreen(@Nullable Screen screen) {
        if (this.screen != null) {
            this.screen.onClose();
        }

        this.screen = screen;

        if (this.screen != null) {
            this.screen.init(this);
            this.screen.onDisplay();
        }
    }

    public void connect() throws InterruptedException {
        this.connection = new NetworkConnection();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(Util.CLIENT_EVENT_GROUP.get())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(@NotNull Channel channel) throws Exception {
                            try {
                                channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                            } catch (ChannelException e) {
                                e.printStackTrace();
                            }

                            NetworkConnection.addHandlers(channel.pipeline());
                            Client.this.connection.setPacketListener(new ClientNetworkHandler(Client.this, Client.this.connection));
                            channel.pipeline().addLast("packet_handler", Client.this.connection);
                        }
                    });

            bootstrap.connect("localhost", 8080).sync();
            this.messages.add("Connected to localhost:8080!".toUpperCase(Locale.ROOT));
            this.setScreen(new ChatScreen());
        } catch (Exception e) {
            this.setScreen(new DisconnectedScreen("COULD NOT CONNECT"));
            this.connection = null;
            this.messages.clear();
        }
    }

    public void scheduleShutdown() {
        this.running = false;
    }

    @Override
    public void destroy() {
        this.shaderManager.destroy();
        Util.shutdownWorkers();
        this.textureManager.destroy();
        this.window.destroy();
    }

    @Override
    public void onSizeChanged() {
        GL11.glViewport(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
    }

    @Override
    public void onFocusChanged() {
    }

    @Override
    public void onMouseButton(int button, int action, int modifiers) {
    }

    @Override
    public void onCursorPos(double xpos, double ypos) {
    }

    @Override
    public void onScroll(double xoffset, double yoffset) {
    }

    @Override
    public void onFileDrop(List<File> files) {
    }

    @Override
    public void onKey(int key, int scancode, int action, int modifiers) {
        if (action != GLFW.GLFW_RELEASE) {
            this.screen.keyPressed(key);
        } else {
            this.screen.keyReleased(key);
        }
    }

    @Override
    public void onCharTyped(int codepoint) {
        this.screen.charTyped(codepoint);
    }

    @Override
    public Thread getMainThread() {
        return this.clientThread;
    }

    public static Client getInstance() {
        return INSTANCE;
    }
}
