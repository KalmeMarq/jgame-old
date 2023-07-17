package me.kalmemarq.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.kalmemarq.common.*;
import me.kalmemarq.common.packet.*;
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

public class Main {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        List<NetworkConnection> connections = new ArrayList<>();

        try {
            JFrame frame = new JFrame("JGame Server");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JTextArea chatArea = new JTextArea();
            chatArea.setPreferredSize(new Dimension(300, 300));
            chatArea.setSize(new Dimension(300, 300));
            chatArea.setMinimumSize(new Dimension(300, 300));
            chatArea.setEditable(false);
            panel.add(chatArea);

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
                                    chatArea.append("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage() + "\n");
                                    connection.sendPacket(packet);
                                }

                                @Override
                                public void onDisconnectPacket(DisconnectPacket packet) {
                                }

                                @Override
                                public void onCommandC2SPacket(CommandC2SPacket packet) {
                                    if (packet.getCommand().startsWith("PLAYERCOUNT")) {
                                        connection.sendPacket(new MessagePacket("Server", Instant.now(), "Player Count: " + connections.size()));
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
            Channel channel = bootstrap.bind("localhost", 8080).sync().channel();

            JTextField textField = new JTextField(30);
            panel.add(textField);
            frame.add(panel);
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

                        if (line.length() > 0) {
                            chatArea.append(line + "\n");
                            textField.setText("");

                            if (line.startsWith("/exit")) {
                                System.out.println("Closing server...");
                                try {
                                    channel.close().sync();
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                } finally {
                                    bossGroup.shutdownGracefully().syncUninterruptibly();
                                    workerGroup.shutdownGracefully().syncUninterruptibly();
                                    frame.dispose();
                                }
                            } else if (line.startsWith("/msg ") || !line.startsWith("/")) {
                                for (NetworkConnection connection : connections) {
                                    connection.sendPacket(new MessagePacket("Server", Instant.now(), line.substring(4)));
                                }
                            } else if (line.startsWith("/playercount")) {
                                chatArea.append("Player Count: " + connections.size() + "\n");
                            } else if (line.startsWith("/kickall")) {
                                for (Iterator<NetworkConnection> it = connections.iterator(); it.hasNext();) {
                                    NetworkConnection connection = it.next();
                                    connection.sendPacket(new DisconnectPacket("YOU WERE KICKED!"));
                                    it.remove();
                                }
                            }
                        }
                    }
                }
            });

            frame.pack();

            frame.setVisible(true);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//            for (;;) {
//                System.out.print("> ");
//                String line = reader.readLine();
//                if (line == null) {
//                   break;
//                }
//
//                if (line.startsWith("/exit")) {
//                    System.out.println("Closing server...");
//                    channel.close().sync();
//                    break;
//                } else if (line.startsWith("/msg ")) {
//                    for (NetworkConnection connection : connections) {
//                        connection.sendPacket(new MessagePacket("Server", Instant.now(), line.substring(4)));
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
        }
    }
}
