package me.kalmemarq.jgame.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.network.packet.CommandC2SPacket;
import me.kalmemarq.jgame.common.network.packet.DisconnectPacket;
import me.kalmemarq.jgame.common.network.packet.HandshakeC2SPacket;
import me.kalmemarq.jgame.common.network.packet.MessagePacket;
import me.kalmemarq.jgame.common.network.packet.Packet;
import me.kalmemarq.jgame.common.network.packet.PingPacket;
import me.kalmemarq.jgame.common.network.packet.PlaySoundS2CPacket;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Server extends me.kalmemarq.jgame.common.Server implements Destroyable {
    private static final int PROTOCOL_VERSION = 100;
    public List<NetworkConnection> connections = new ArrayList<>();
    Channel channel;
    private MessageListener listener;

    private Thread serverThread;
    private ServerGui gui;
    private boolean running;

    CommandDispatcher<ServerCommandSource> dispatcher;
    static class ServerCommandSource {
        NetworkConnection connection;
    }

    static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<ServerCommandSource, T> required(String name, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(name, argumentType);
    }
    
    public Server(Thread serverThread, MessageListener listener) {
        this.serverThread = serverThread;
        this.listener = listener;
        this.dispatcher = new CommandDispatcher<>();
    }

    @Override
    public Thread getRequiredThread() {
        return this.serverThread;
    }

    public void init() throws InterruptedException {
        this.dispatcher.register(literal("PLAYERCOUNT").executes(c -> {
            c.getSource().connection.sendPacket(new MessagePacket("Server", Instant.now(), "Player Count: " + this.connections.size()));
            return 0;
        }));

        this.dispatcher.register(
            literal("PLAYSOUND")
                .executes(ctx -> {

                    ctx.getSource().connection.sendPacket(new PlaySoundS2CPacket("assets/minicraft/sounds/select.ogg", 1.0f, 1.0f));

                    return 0;
                })
        );

        ServerBootstrap bootstrap = new ServerBootstrap()
            .group(NetworkConnection.SERVER_EVENT_GROUP.get())
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(@NotNull Channel ch) throws Exception {
                    NetworkConnection connection = new NetworkConnection();
                    connections.add(connection);
                    connection.setPacketListener(new Packet.ServerPacketListener() {
                        @Override
                        public void onMessagePacket(MessagePacket packet) {
                            listener.onMessage("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage() + "\n");
                            connection.sendPacket(packet);
                        }

                        @Override
                        public void onDisconnectPacket(DisconnectPacket packet) {
                        }

                        @Override
                        public void onCommandC2SPacket(CommandC2SPacket packet) {
                            ServerCommandSource source = new ServerCommandSource();
                            source.connection = connection;
                            try {
                                dispatcher.execute(packet.getCommand(), source);
                            } catch (CommandSyntaxException e) {
                                connection.sendPacket(new MessagePacket("Server", Instant.now(), e.getMessage().toUpperCase(Locale.ROOT)));
                            }
                        }

                        @Override
                        public void onHandshakeC2SPacket(HandshakeC2SPacket packet) {
                            System.out.println(packet.getProtocolVersion());
                            if (packet.getProtocolVersion() != PROTOCOL_VERSION) {
                                listener.onMessage("Client with wrong protocol version tried to connect!");
                                connection.sendPacket(new DisconnectPacket("INCOMPATIBLE PROTOCOL VERSION!"));
                                connection.disconnect();
                                Server.this.connections.remove(connection);
                            }
                        }

                        @Override
                        public void onPlaySoundPacket(PlaySoundS2CPacket packet) {
                            connection.sendPacket(packet);
                        }

                        @Override
                        public void onDisconnected() {
                        }

                        @Override
                        public void onPingPacket(PingPacket packet) {
                            connection.sendPacket(packet);
                        }
                    });
                    System.out.println("new client connected");
                    NetworkConnection.addHandlers(ch.pipeline());
                    ch.pipeline().addLast("packet_handler", connection);
                }
            });
        this.channel = bootstrap.bind("localhost", 8080).sync().channel();
    }

    public interface MessageListener {
        void onMessage(String msg);
    }
    
    public void createGui() {
        this.gui = new ServerGui(this);
        this.listener = this.gui.chatArea::append;
    }
    
    private int ticks;

    public void run() {
        try {
            long lastTimeTick = System.nanoTime();
            int tickCounter = 0;
            double unprocessed = 0;
            long lastTime = System.currentTimeMillis();
            this.running = true;
            
            while (this.running) {
                long now = System.nanoTime();
                double nsPerTick = 1E9D / 20;
                unprocessed += (now - lastTimeTick) / nsPerTick;
                lastTimeTick = now;
                while (unprocessed >= 1) {
                    tickCounter++;
                    this.tick();
                    unprocessed--;
                }

                while (System.currentTimeMillis() - lastTime > 1000L) {
                    this.ticks = tickCounter;
                    tickCounter = 0;
                    if (this.gui != null) this.gui.setTitle("JGame Server " + this.ticks + " TPS");
                    lastTime += 1000L;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.destroy();
        }
    }

    public int getTicks() {
        return this.ticks;
    }

    @Override
    public boolean isDedicated() {
        return true;
    }

    @Override
    public void destroy() {
        this.running = false;
        if (this.gui != null) {
            this.gui.stop();
        }
        System.out.println("Closing server...");
        try {
            this.channel.close().sync();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void stop() {
        this.running = false;
        try {
            this.serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    static class ServerGui {
        private final Server server;
        private final JFrame frame;
        JTextArea chatArea;

        public ServerGui(Server server) {
            this.server = server;
            this.frame = new JFrame("JGame Server");
            this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            this.chatArea = new JTextArea();
            this.chatArea.setPreferredSize(new Dimension(300, 300));
            this.chatArea.setSize(new Dimension(300, 300));
            this.chatArea.setMinimumSize(new Dimension(300, 300));
            this.chatArea.setEditable(false);
            panel.add(this.chatArea);

            JTextField textField = new JTextField(30);
            panel.add(textField);
            this.frame.add(panel);
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });

            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    super.keyPressed(e);

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String line = textField.getText();

                        if (!line.isEmpty()) {
                            chatArea.append(line + "\n");
                            textField.setText("");

                            if (line.startsWith("/exit")) {
                                server.stop();
                            } else if (line.startsWith("/msg ")) {
                                for (NetworkConnection connection : server.connections) {
                                    connection.sendPacket(new MessagePacket("Server", Instant.now(), line.substring(4)));
                                }
                            } else if (line.startsWith("/playsound")) {
                                for (NetworkConnection connection : server.connections) {
                                    connection.sendPacket(new PlaySoundS2CPacket("assets/minicraft/sounds/select.ogg", 1.0f, 1.0f));
                                }
                            } else if (line.startsWith("/playercount")) {
                                chatArea.append("Player Count: " + server.connections.size() + "\n");
                            } else if (line.startsWith("/kickall")) {
                                for (Iterator<NetworkConnection> it = server.connections.iterator(); it.hasNext();) {
                                    NetworkConnection connection = it.next();
                                    connection.sendPacket(new DisconnectPacket("YOU WERE KICKED!"));
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            });

            this.frame.pack();

            this.frame.setVisible(true);
        }
        
        public void setTitle(String title) {
            this.frame.setTitle(title);
        }

        public void stop() {
            this.frame.dispose();
        }
    }
}
