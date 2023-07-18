package me.kalmemarq.jgame.common;

import io.netty.channel.nio.NioEventLoopGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Util {
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
