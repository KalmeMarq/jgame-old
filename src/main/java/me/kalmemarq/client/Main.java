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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
        client.destroy();
        System.exit(0);
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
                                public void onPingPacket(PingPacket packet) {
                                }

                                @Override
                                public void onCommandC2SPacket(CommandC2SPacket packet) {
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