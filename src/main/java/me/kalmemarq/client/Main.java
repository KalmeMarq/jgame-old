package me.kalmemarq.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.kalmemarq.common.DisconnectPacket;
import me.kalmemarq.common.MessagePacket;
import me.kalmemarq.common.NetworkConnection;
import me.kalmemarq.common.Packet;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        Window window = new Window(800, 600, "JGame");
        window.init();

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        while (!window.shouldClose()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            window.swapBuffers();
        }

        window.destroy();
    }

    public static void main2(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            NetworkConnection connection = new NetworkConnection();

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
                                    System.out.println("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage());
                                }

                                @Override
                                public void onDisconnectPacket(DisconnectPacket packet) {
                                    connection.disconnect();
                                }

                                @Override
                                public void onDisconnected() {
                                }
                            });
                            channel.pipeline().addLast("packet_handler", connection);
                        }
                    });

            bootstrap.connect("localhost", 8080).sync();

            System.out.println("Connected to localhost:8080!");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            for (;;) {
                System.out.print("> ");
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("/quit")) {
                    connection.disconnect();
                    break;
                } else {
                    connection.sendPacket(new MessagePacket("KalmeMarq", Instant.now(), line));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().syncUninterruptibly();
        }
    }
}