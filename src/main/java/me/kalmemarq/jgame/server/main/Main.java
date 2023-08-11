package me.kalmemarq.jgame.server.main;

import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.network.packet.DisconnectPacket;
import me.kalmemarq.jgame.common.network.packet.MessagePacket;
import me.kalmemarq.jgame.common.network.packet.PlaySoundS2CPacket;
import me.kalmemarq.jgame.server.Server;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
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

            frame.pack();

            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
