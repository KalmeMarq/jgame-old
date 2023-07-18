package me.kalmemarq.jgame.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ThreadDeathWatcher;
import me.kalmemarq.jgame.common.NetworkConnection;
import me.kalmemarq.jgame.common.Util;
import me.kalmemarq.jgame.common.packet.CommandC2SPacket;
import me.kalmemarq.jgame.common.packet.DisconnectPacket;
import me.kalmemarq.jgame.common.packet.MessagePacket;
import me.kalmemarq.jgame.common.packet.Packet;
import me.kalmemarq.jgame.common.packet.PingPacket;
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

            Server server = new Server(Thread.currentThread(), chatArea::append);
            server.run();

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
                                    server.destroy();
                                } finally {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
