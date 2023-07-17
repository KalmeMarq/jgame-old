package me.kalmemarq.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.kalmemarq.common.DisconnectPacket;
import me.kalmemarq.common.MessagePacket;
import me.kalmemarq.common.NetworkConnection;
import me.kalmemarq.common.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        List<NetworkConnection> connections = new ArrayList<>();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(@NotNull Channel ch) throws Exception {
                            NetworkConnection connection = new NetworkConnection();
                            connections.add(connection);
                            connection.setPacketListener(new Packet.PacketListener() {
                                @Override
                                public void onMessagePacket(MessagePacket packet) {
                                    System.out.println("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage());
                                    connection.sendPacket(packet);
                                }

                                @Override
                                public void onDisconnectPacket(DisconnectPacket packet) {
                                }

                                @Override
                                public void onDisconnected() {
                                }
                            });
                            System.out.println("new client connected");
                            NetworkConnection.addHandlers(ch.pipeline());
                            ch.pipeline().addLast("packet_handler", connection);
                        }
                    });
            Channel channel = bootstrap.bind("localhost", 8080).sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            for (;;) {
                System.out.print("> ");
                String line = reader.readLine();
                if (line == null) {
                   break;
                }

                if (line.startsWith("/exit")) {
                    System.out.println("Closing server...");
                    channel.close().sync();
                    break;
                } else if (line.startsWith("/msg ")) {
                    for (NetworkConnection connection : connections) {
                        connection.sendPacket(new MessagePacket("Server", Instant.now(), line.substring(4)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
