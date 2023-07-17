package me.kalmemarq.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.kalmemarq.common.*;
import me.kalmemarq.common.packet.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Client implements Destroyable, Window.WindowEventHandler, Window.MouseEventHandler, Window.KeyboardEventHandler {
    private final Window window;
    public final Font font;
    public final TextureManager textureManager;
    private boolean running;
    private String input = "";
    NetworkConnection connection;
    private List<String> messages = new ArrayList<>();
    private int gameState = -1;
    private String disconnectReason = "";

    public Client() {
        this.window = new Window(800, 600, "JGame");
        this.textureManager = new TextureManager();
        this.font = new Font();
    }

    public void run() {
        System.setOut(new LoggerPrintStream("SystemOut", System.out, false, false));
        System.setErr(new LoggerPrintStream("SystemError", System.err, true, false));

        this.window.init(this, this, this);
        this.running = true;
        this.font.load();
        Renderer.enableTexture();
        this.textureManager.bindTexture("/font.png");

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        try {
            long lastTime = System.currentTimeMillis();
            int frameCounter = 0;

            while (this.running) {
                if (this.window.shouldClose()) this.running = false;

                Renderer.clear(true, true, false);

                Renderer.matrixMode(Renderer.MatrixMode.PROJECTION);
                Renderer.loadIdentity();
                Renderer.ortho(0, this.window.getFramebufferWidth() / 2d, this.window.getFramebufferHeight() / 2d, 0, 1000, 3000);
                Renderer.matrixMode(Renderer.MatrixMode.MODELVIEW);
                Renderer.loadIdentity();
                Renderer.translate(0, 0, -2000.0f);

                if (this.gameState == -1) {
                    this.font.drawText("IP: LOCALHOST:8080", 10, 10, 0xFF_FFFFFF);
                    this.font.drawText("USERNAME: KALMEMARQ", 10, 20, 0xFF_FFFFFF);

                    this.font.drawText("CONNECT", 10, 40, 0xFF_FFFFFF);

                    if (GLFW.glfwGetKey(this.window.getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS) {
                        this.connect();
                    }
                } else if (this.gameState == 0) {
                    this.font.drawText("CONNECTING...", this.window.getScaledWidth() / 2 - this.font.textWidth("CONNECTING...") / 2, this.window.getScaledHeight() / 2 - 4, 0xFF_FFFFFF);
                } else if (this.gameState == 1) {
                    int y = this.window.getScaledHeight() - 20 - 2;
                    for (int i = this.messages.size() - 1; i >= 0; --i) {
                        List<String> l = this.font.breakTextIntoLines(this.messages.get(i), this.window.getScaledWidth() - 10);

                        for (int j = l.size() - 1; j >= 0; --j) {
                            this.font.drawText(l.get(j), 1, y, 0xFFEEEEEE);

                            y -= 10;
                        }
                    }

                    Renderer.disableTexture();
                    Renderer.begin(Renderer.PrimitiveType.QUADS);
                    Renderer.color(1.0f, 1.0f, 1.0f, 1.0f);
                    Renderer.vertex(0, this.window.getScaledHeight() - 13, 0);
                    Renderer.vertex(0, this.window.getScaledHeight() - 12, 0);
                    Renderer.vertex(this.window.getScaledWidth(), this.window.getScaledHeight() - 12, 0);
                    Renderer.vertex(this.window.getScaledWidth(), this.window.getScaledHeight() - 13, 0);
                    Renderer.end();
                    Renderer.enableTexture();

                    this.font.drawText(">" + this.input, 1, this.window.getScaledHeight() - 10, 0xFF_FFFFFF);
                } else if (this.gameState == 2) {
                    if (GLFW.glfwGetKey(this.window.getHandle(), GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS) {
                        gameState = -1;
                    }
                    this.font.drawText(this.disconnectReason, this.window.getScaledWidth() / 2 - this.font.textWidth(this.disconnectReason) / 2, this.window.getScaledHeight() / 2 - 4, 0xFF_FFFFFF);
                }

                this.window.swapBuffers();

                ++frameCounter;

                while (System.currentTimeMillis() - lastTime > 1000L) {
                    this.window.setTitle("Client " + frameCounter + " FPS ");
                    frameCounter = 0;
                    lastTime += 1000L;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.running = false;
        }
    }

    private void connect() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        this.connection = new NetworkConnection();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
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
                            connection.setPacketListener(new Packet.PacketListener() {
                                @Override
                                public void onMessagePacket(MessagePacket packet) {
                                    messages.add(("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage()).toUpperCase(Locale.ROOT));
                                }

                                @Override
                                public void onCommandC2SPacket(CommandC2SPacket packet) {
                                }

                                @Override
                                public void onDisconnectPacket(DisconnectPacket packet) {
                                    System.out.println("disc");
                                    disconnectReason = packet.getReason();
                                    connection.disconnect();
                                    gameState = 2;
                                    connection = null;
                                    messages.clear();
                                }

                                @Override
                                public void onPingPacket(PingPacket packet) {
                                    messages.add(("Your ping is " + (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - packet.getNanoTime())) + " ms").toUpperCase(Locale.ROOT));
                                }

                                @Override
                                public void onDisconnected() {
                                }
                            });
                            channel.pipeline().addLast("packet_handler", connection);
                        }
                    });

            bootstrap.connect("localhost", 8080).sync();
            this.messages.add("Connected to localhost:8080!".toUpperCase(Locale.ROOT));
            this.gameState = 1;
        } catch (Exception e) {
            group.shutdownGracefully();
            gameState = 2;
            connection = null;
            messages.clear();
            disconnectReason = "COULD NOT CONNECT";
        }
    }

    public void scheduleShutdown() {
        this.running = false;
    }

    @Override
    public void destroy() {
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
        if (this.gameState != 1) return;

        if (key == GLFW.GLFW_KEY_BACKSPACE && GLFW.glfwGetKey(this.window.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS && action != GLFW.GLFW_RELEASE) {
            this.input = "";
        } else if (key == GLFW.GLFW_KEY_BACKSPACE && action != GLFW.GLFW_RELEASE) {
            if (this.input.length() > 0) {
                this.input = this.input.substring(0, this.input.length() - 1);
            }
        } else if (key == GLFW.GLFW_KEY_ENTER && action != GLFW.GLFW_PRESS) {
             if (this.input.startsWith("/PING")) {
                 this.connection.sendPacket(new PingPacket(System.nanoTime()));
                 this.input = "";
             } else if (this.input.startsWith("/QUIT")) {
                 this.connection.disconnect();
                 this.scheduleShutdown();
                 this.input = "";
             } else if (this.input.startsWith("/")) {
                 this.connection.sendPacket(new CommandC2SPacket(this.input.substring(1)));
                 this.input = "";
             } else {
                 this.connection.sendPacket(new MessagePacket("KalmeMarq", Instant.now(), this.input));
                 this.input = "";
             }
         }
    }

    @Override
    public void onCharTyped(int codepoint) {
        this.input += Character.toString(codepoint).toUpperCase(Locale.ROOT);
    }
}
