package me.kalmemarq.jgame.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.kalmemarq.jgame.client.render.*;
import me.kalmemarq.jgame.client.render.shader.ShaderManager;
import me.kalmemarq.jgame.client.render.texture.TextureManager;
import me.kalmemarq.jgame.client.screen.ScreenStack;
import me.kalmemarq.jgame.client.screen.TitleScreen;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.MemoryUnit;
import me.kalmemarq.jgame.common.ThreadExecutor;
import me.kalmemarq.jgame.common.ThreadUtils;
import me.kalmemarq.jgame.common.network.ClientNetworkHandler;
import me.kalmemarq.jgame.client.resource.ResourceLoader;
import me.kalmemarq.jgame.client.resource.ResourceManager;
import me.kalmemarq.jgame.client.screen.ChatScreen;
import me.kalmemarq.jgame.client.screen.DisconnectedScreen;
import me.kalmemarq.jgame.client.screen.Screen;
import me.kalmemarq.jgame.client.sound.SoundManager;
import me.kalmemarq.jgame.common.logger.Logger;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.network.packet.HandshakeC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Client extends ThreadExecutor<Runnable> implements Destroyable, Window.WindowEventHandler {
    private static Client INSTANCE;
    private static final Logger LOGGER = Logger.getLogger();
    
    private final Thread clientThread;
    public final Window window;
    public final Font font;
    public final SoundManager soundManager;
    public final TextureManager textureManager;
    private boolean running;
    public Settings options;
    public NetworkConnection connection;
    public List<String> messages = new ArrayList<>();
    private final ResourceLoader resourceLoader;
    private final ShaderManager shaderManager;
    private final Framebuffer framebuffer;
    private final ResourceManager resourceManager = new ResourceManager();
    private final Path gameSavePath;
    public final ScreenStack screenStack = new ScreenStack(this);
    AllocationRateCalculator allocationRateCalculator;
    protected ImGuiLayer imGuiLayer;
    private final MouseHandler mouseHandler;
    private final KeyboardHandler keyboardHandler;

    public Client(Path gameSavePath, boolean debugImGui, boolean debugLWJGL) {
        INSTANCE = this;
        this.allocationRateCalculator = new AllocationRateCalculator();
        this.gameSavePath = gameSavePath;
        this.clientThread = Thread.currentThread();
        this.options = new Settings(gameSavePath);
        this.options.load();
        this.options.debugLWJGL = debugLWJGL;
        this.options.debugImGui = debugImGui;
        this.window = new Window(288 * 3, 192 * 3, "JGame", this.options.vsync.getValue());
        this.soundManager = new SoundManager(this.resourceManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.font = new Font();
        this.resourceLoader = new ResourceLoader();
        this.shaderManager = new ShaderManager();
        this.framebuffer = new Framebuffer();
        if (this.options.debugImGui) {
            this.imGuiLayer = new ImGuiLayer(this);
        }
        this.mouseHandler = new MouseHandler(this);
        this.keyboardHandler = new KeyboardHandler(this);
    }
    
    protected void takeScreenshot() {
        Path screenshotDir = Paths.get(this.gameSavePath.toString(), "screenshots");

        try {
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create screenshots folder: {}", e);
        }
    }
    
    private void reloadResources() {
        this.resourceLoader.start(List.of(this.shaderManager, this.textureManager, this.font), ThreadUtils.RELOADER_WORKER.get(), this, this.resourceManager, () -> {
        });
    }

    public void run() {
        try {
            this.window.init(this, this.mouseHandler, this.keyboardHandler);
        } catch (Exception e) {
            LOGGER.fatal("{}", e);
        }
        
        this.framebuffer.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        
        MemoryUtils.setupAllocator(this.options.debugLWJGL);
        if (this.options.debugImGui) {
            this.imGuiLayer.init();
        }
        this.soundManager.init();
        this.running = true;


        System.out.println(GL11.glGetString(GL11.GL_VENDOR));
        System.out.println(GL11.glGetString(GL11.GL_RENDERER));
        System.out.println(GL11.glGetString(GL11.GL_VERSION));

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        this.reloadResources();
        
        try {
            long lastTime = System.currentTimeMillis();
            long lastTimeTick = System.nanoTime();
            int frameCounter = 0;
            int tickCounter = 0;
            double unprocessed = 0;

            this.screenStack.push(new TitleScreen());
            while (this.running) {
                if (this.window.shouldClose()) this.running = false;

                this.runTasks();

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
//                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                this.render();
//                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

                this.framebuffer.end();
                
                Renderer.clear(true, false, false);
                Renderer.setProjectionMatrixIdentity();
                Renderer.setModelViewMatrixIdentity();
//
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
            LOGGER.fatal("{}", e);
        } finally {
            this.running = false;
        }
     
        this.options.save();
        this.soundManager.destroy();
        this.framebuffer.destroy();
    }

    private void tick() {
        this.soundManager.tick();
    }
    
    private void render() {
        Renderer.setProjectionMatrix(Renderer.getProjectionMatrix().identity().setOrtho(0, this.window.getFramebufferWidth() / 3f, this.window.getFramebufferHeight() / 3f, 0, 1000, 3000));
        Renderer.setModeViewMatrix(Renderer.getModeViewMatrix().identity().translate(0, 0, -2000.0f));
        Renderer.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        DrawContext context = new DrawContext(this);
        
        if (this.resourceLoader.isActive()) {
            if (ShaderManager.hasLoadedInitial()) {
                context.drawColoured(0, this.window.getScaledHeight() - 23, 0, (int) (this.window.getScaledWidth() * this.resourceLoader.getProgress()), 12, 0xFF_FFFFFF);
                context.drawString("Progress: " + (int)(this.resourceLoader.getProgress() * 100) + "%", 1, this.window.getScaledHeight() - 40, 0xFF_FFFFFF);
            }
        } else {
            

//            if (this.options.debugImGui) this.imGuiLayer.render();

            this.screenStack.render(context);

            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long availMem = totalMem - freeMem;

            String[] texts = {
                "Java: " + System.getProperty("java.version"),
                String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", availMem * 100L / maxMem, (long) MemoryUnit.BYTES.toMiB(availMem), (long) MemoryUnit.BYTES.toMiB(maxMem)),
                String.format(Locale.ROOT, "Allocation rate: %dMB /s", (long) MemoryUnit.BYTES.toMiB(this.allocationRateCalculator.get(availMem))),
                String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", totalMem * 100L / maxMem, (long) MemoryUnit.BYTES.toMiB(totalMem))
            };
            int y = 1;

            context.getMatrices().push();
            context.getMatrices().scale(0.6666f, 0.6666f, 0.6666f);

            Renderer.enableBlend();
            Renderer.blendFunction(Renderer.BlendFactor.SRC_ALPHA, Renderer.BlendFactor.ONE_MINUS_SRC_ALPHA);

            for (int i = 0; i < texts.length; ++i) {
                context.drawStringM(texts[i], 0, y, 0xFF_FFFFFF);
                y += 9;
            }

            Renderer.disableBlend();

            context.getMatrices().pop();
        }
    }

    @Deprecated
    public void setScreen(@Nullable Screen screen) {
        this.screenStack.popAll();
        this.screenStack.push(screen);
    }

    public void connect() throws InterruptedException {
        this.connection = new NetworkConnection();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(NetworkConnection.CLIENT_EVENT_GROUP.get())
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
            this.connection.sendPacket(new HandshakeC2SPacket(2));
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
        ThreadUtils.shutdownExecutors();
        if (this.options.debugImGui) {
            this.imGuiLayer.destroy();
        }
        this.shaderManager.destroy();
        this.textureManager.destroy();
        MemoryUtils.cleanup();
        this.window.destroy();
    }

    @Override
    public void onSizeChanged() {
        LOGGER.debug("Resizing framebuffer");
        this.framebuffer.resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        GL11.glViewport(0, 0, this.window.getFramebufferWidth(), this.window.getFramebufferHeight());
        this.screenStack.resize(this.window.getScaledWidth(), this.window.getScaledHeight());
    }

    @Override
    public void onFocusChanged() {
    }

    @Override
    public Runnable createTask(Runnable runnable) {
        return runnable;
    }

    @Override
    public Thread getRequiredThread() {
        return this.clientThread;
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    static class AllocationRateCalculator {
        private static final List<GarbageCollectorMXBean> GARBAGE_COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
        private long lastCalculated = 0L;
        private long allocatedBytes = -1L;
        private long collectionCount = -1L;
        private long allocationRate = 0L;

        AllocationRateCalculator() {
        }

        long get(long allocatedBytes) {
            long m = System.currentTimeMillis();
            if (m - this.lastCalculated < 500L) {
                return this.allocationRate;
            }
            long n = AllocationRateCalculator.getCollectionCount();
            if (this.lastCalculated != 0L && n == this.collectionCount) {
                double d = (double) TimeUnit.SECONDS.toMillis(1L) / (double)(m - this.lastCalculated);
                long o = allocatedBytes - this.allocatedBytes;
                this.allocationRate = Math.round((double)o * d);
            }
            this.lastCalculated = m;
            this.allocatedBytes = allocatedBytes;
            this.collectionCount = n;
            return this.allocationRate;
        }

        private static long getCollectionCount() {
            long l = 0L;
            for (GarbageCollectorMXBean garbageCollectorMXBean : GARBAGE_COLLECTORS) {
                l += garbageCollectorMXBean.getCollectionCount();
            }
            return l;
        }
    }
}
