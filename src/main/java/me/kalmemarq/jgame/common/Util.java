package me.kalmemarq.jgame.common;

import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Util {
    public static final Lazy<ExecutorService> RELOADER_WORKER = new Lazy<>(() -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    public static final Lazy<ExecutorService> IO_WORKER = new Lazy<>(() -> Executors.newCachedThreadPool((runnable) -> new Thread(runnable, "IO Worker")));
    
    public static void shutdownExecutors() {
        RELOADER_WORKER.ifInitialized(Util::shutdownExecutor);
        IO_WORKER.ifInitialized(Util::shutdownExecutor);
    }
    
    private static void shutdownExecutor(ExecutorService service) {
        service.shutdown();

        boolean terminated;
        try {
            terminated = service.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            terminated = false;
        }

        if (!terminated) {
            service.shutdownNow();
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

    public static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }
}
