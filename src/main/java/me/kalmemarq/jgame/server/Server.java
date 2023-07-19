package me.kalmemarq.jgame.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.kalmemarq.jgame.common.Destroyable;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.Util;
import me.kalmemarq.jgame.common.network.packet.CommandC2SPacket;
import me.kalmemarq.jgame.common.network.packet.DisconnectPacket;
import me.kalmemarq.jgame.common.network.packet.MessagePacket;
import me.kalmemarq.jgame.common.network.packet.Packet;
import me.kalmemarq.jgame.common.network.packet.PingPacket;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Server implements Destroyable {
    List<NetworkConnection> connections = new ArrayList<>();
    Channel channel;
    private MessageListener listener;

    private Thread serverThread;

    CommandDispatcher<ServerCommandSource> dispatcher;
    static class ServerCommandSource {
        NetworkConnection connection;
    }

    static LiteralArgumentBuilder<ServerCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public Server(Thread serverThread, MessageListener listener) throws InterruptedException {
        this.serverThread = serverThread;
        this.listener = listener;
        this.dispatcher = new CommandDispatcher<>();

        this.dispatcher.register(literal("PLAYERCOUNT").executes(c -> {
            c.getSource().connection.sendPacket(new MessagePacket("Server", Instant.now(), "Player Count: " + this.connections.size()));
            return 0;
        }));

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(Util.SERVER_EVENT_GROUP.get())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(@NotNull Channel ch) throws Exception {
                        NetworkConnection connection = new NetworkConnection();
                        connections.add(connection);
                        connection.setPacketListener(new Packet.PacketListener() {
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
        channel = bootstrap.bind("localhost", 8080).sync().channel();
    }

    interface MessageListener {
        void onMessage(String msg);
    }

    public void run() {
    }

    public void tick() {
    }

    @Override
    public void destroy() {
        System.out.println("Closing server...");
        try {
            this.channel.close().sync();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
