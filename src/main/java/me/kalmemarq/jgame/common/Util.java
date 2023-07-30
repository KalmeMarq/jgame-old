package me.kalmemarq.jgame.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Util {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
    
    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    public static String readString(InputStream stream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (Exception ignored) {
        }
        return builder.toString();
    }
    
    public static boolean isValidString(String str, Predicate<Character> predicate) {
        for (int i = 0; i < str.length(); ++i) {
            if (!predicate.test(str.charAt(i))) return false;
        }
        return true;
    }
    
    public static int[] arrayNodeToIntArray(ArrayNode arrayNode) {
        int[] arr = new int[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); ++i) {
            arr[i] = arrayNode.get(i).intValue();
        }
        return arr;
    }

    public static float[] arrayNodeToFloatArray(ArrayNode arrayNode) {
        float[] arr = new float[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); ++i) {
            arr[i] = arrayNode.get(i).floatValue();
        }
        return arr;
    }
}
