package me.kalmemarq.jgame.common;

import io.netty.channel.nio.NioEventLoopGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Util {
    public static final ExecutorService RELOADER_WORKER = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void shutdownWorkers() {
        RELOADER_WORKER.shutdown();

        boolean terminated;
        try {
            terminated = RELOADER_WORKER.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            terminated = false;
        }

        if (!terminated) {
            RELOADER_WORKER.shutdownNow();
        }
    }

    public static final Lazy<NioEventLoopGroup> CLIENT_EVENT_GROUP = new Lazy<>(() -> new NioEventLoopGroup(0, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Netty Client IO");
        thread.setDaemon(true);
        return thread;
    }));
    public static final Lazy<NioEventLoopGroup> SERVER_EVENT_GROUP = new Lazy<>(() -> new NioEventLoopGroup(0, runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Netty Server IO");
        thread.setDaemon(true);
        return thread;
    }));

    public static String readString(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception ignored) {
        }
        return builder.toString();
    }
}
