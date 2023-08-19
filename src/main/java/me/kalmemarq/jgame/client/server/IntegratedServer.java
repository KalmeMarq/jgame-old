package me.kalmemarq.jgame.client.server;

import me.kalmemarq.jgame.client.Client;
import me.kalmemarq.jgame.common.Server;

public class IntegratedServer extends Server {
    Thread serverThread;
    
    public IntegratedServer(Thread serverThread, Client client) {
        this.serverThread = serverThread;
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    @Override
    public Thread getRequiredThread() {
        return this.serverThread;
    }
}
