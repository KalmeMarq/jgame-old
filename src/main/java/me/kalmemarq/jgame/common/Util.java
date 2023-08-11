package me.kalmemarq.jgame.common;

import me.kalmemarq.jgame.common.logger.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Util {
    private static final Logger LOGGER = Logger.getLogger("Util");
    public static final Lazy<ExecutorService> RELOADER_WORKER = new Lazy<>(() -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    
    public static void shutdownExecutors() {
        RELOADER_WORKER.ifInitialized(Util::shutdownExecutor);
    }
    
    private static void shutdownExecutor(ExecutorService service) {
        service.shutdown();

        boolean terminated;
        try {
            terminated = service.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Unable to terminate service: {}", e);
            terminated = false;
        }

        if (!terminated) {
            service.shutdownNow();
        }
    }
}
