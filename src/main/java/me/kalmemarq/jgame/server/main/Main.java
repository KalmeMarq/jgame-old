package me.kalmemarq.jgame.server.main;

import me.kalmemarq.jgame.server.Server;

import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        AtomicReference<Server> server = new AtomicReference<>();
        Thread serverThread = new Thread(() -> {
            try {
                server.get().init();
                server.get().run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        serverThread.setName("Server Thread");
        server.set(new Server(Thread.currentThread(), (s) -> {}));
        serverThread.start();
        server.get().createGui();
    }
}
