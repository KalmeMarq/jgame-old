package me.kalmemarq.jgame.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.kalmemarq.jgame.client.network.ClientNetworkHandler;
import me.kalmemarq.jgame.client.resource.ResourceLoader;
import me.kalmemarq.jgame.client.resource.SyncResourceReloader;
import me.kalmemarq.jgame.client.screen.ChatScreen;
import me.kalmemarq.jgame.client.screen.ConnectScreen;
import me.kalmemarq.jgame.client.screen.DisconnectedScreen;
import me.kalmemarq.jgame.client.screen.Screen;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.ThreadExecutor;
import me.kalmemarq.jgame.common.logger.LoggerPrintStream;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Client extends ThreadExecutor implements Destroyable, Window.WindowEventHandler, Window.MouseEventHandler, Window.KeyboardEventHandler {
    public final Window window;
    public final Font font;
    public final TextureManager textureManager;
    private boolean running;
    private GameOptions options;
    public NetworkConnection connection;
    public List<String> messages = new ArrayList<>();
    private Screen screen;
    private ResourceLoader resourceLoader;

    private ShaderManager shaderManager;

    public Client(File gameDir) {
        this.options = new GameOptions(gameDir);
        this.options.load();
        this.window = new Window(800, 600, "JGame");
        this.textureManager = new TextureManager();
        this.font = new Font();
        this.resourceLoader = new ResourceLoader();
        this.shaderManager = new ShaderManager();
    }

    public void run() {
        System.setOut(new LoggerPrintStream("SystemOut", System.out, false, false));
        System.setErr(new LoggerPrintStream("SystemError", System.err, true, false));

        this.window.init(this, this, this);
        this.running = true;
        Renderer.enableTexture();
        this.textureManager.bindTexture("/font.png");

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        this.resourceLoader.start(List.of(this.shaderManager, this.textureManager, this.font), Util.RELOADER_WORKER, this, () -> {
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

                Renderer.clear(true, true, false);

                Renderer.matrixMode(Renderer.MatrixMode.PROJECTION);
                Renderer.loadIdentity();
                Renderer.ortho(0, this.window.getFramebufferWidth() / 2d, this.window.getFramebufferHeight() / 2d, 0, 1000, 3000);
                Renderer.matrixMode(Renderer.MatrixMode.MODELVIEW);
                Renderer.loadIdentity();
                Renderer.translate(0, 0, -2000.0f);

                if (this.resourceLoader.isActive()) {
                    Renderer.disableTexture();
                    Renderer.begin(Renderer.PrimitiveType.QUADS);
                    Renderer.color(1.0f, 1.0f, 1.0f, 1.0f);
                    Renderer.vertex(0, this.window.getScaledHeight() - 23, 0);
                    Renderer.vertex(0, this.window.getScaledHeight() - 12, 0);
                    Renderer.vertex(this.window.getScaledWidth() * this.resourceLoader.getProgress(), this.window.getScaledHeight() - 12, 0);
                    Renderer.vertex(this.window.getScaledWidth() * this.resourceLoader.getProgress(), this.window.getScaledHeight() - 23, 0);
                    Renderer.end();
                    Renderer.enableTexture();

                    this.font.drawText("PROGRESS: " + (int)(this.resourceLoader.getProgress() * 100) + "%", 1, this.window.getScaledHeight() - 40, 0xFF_FFFFFF);
                } else {
                    if (this.screen != null) this.screen.render();
                }

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
    }

    public void tick() {
        if (this.screen != null) this.screen.tick();
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
}
