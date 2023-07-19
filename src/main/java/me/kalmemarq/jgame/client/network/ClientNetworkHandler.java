package me.kalmemarq.jgame.client.network;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.client.screen.DisconnectedScreen;
import me.kalmemarq.jgame.common.network.NetworkConnection;
import me.kalmemarq.jgame.common.network.packet.CommandC2SPacket;
import me.kalmemarq.jgame.common.network.packet.DisconnectPacket;
import me.kalmemarq.jgame.common.network.packet.MessagePacket;
import me.kalmemarq.jgame.common.network.packet.Packet;
import me.kalmemarq.jgame.common.network.packet.PingPacket;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClientNetworkHandler implements Packet.PacketListener {
    private final Client client;
    private final NetworkConnection connection;

    public ClientNetworkHandler(Client client, NetworkConnection connection) {
        this.client = client;
        this.connection = connection;
    }

    @Override
    public void onMessagePacket(MessagePacket packet) {
        this.client.messages.add(("[" + packet.getCreatedTime().toString() + "] <" + packet.getUsername() + "> " + packet.getMessage()).toUpperCase(Locale.ROOT));
    }

    @Override
    public void onDisconnectPacket(DisconnectPacket packet) {
        this.client.setScreen(new DisconnectedScreen(packet.getReason()));
        this.connection.disconnect();
        this.client.connection = null;
        this.client.messages.clear();
    }

    @Override
    public void onPingPacket(PingPacket packet) {
        this.client.messages.add(("Your ping is " + (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - packet.getNanoTime())) + " ms").toUpperCase(Locale.ROOT));
    }

    @Override
    public void onCommandC2SPacket(CommandC2SPacket packet) {
    }

    @Override
    public void onDisconnected() {
    }
}