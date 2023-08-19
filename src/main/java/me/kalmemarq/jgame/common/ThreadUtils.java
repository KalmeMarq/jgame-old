package me.kalmemarq.jgame.common;

import me.kalmemarq.jgame.common.logger.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    private static final Logger LOGGER = Logger.getLogger("ThreadUtils");
    public static final Lazy<ExecutorService> RELOADER_WORKER = new Lazy<>(() -> ThreadUtils.createWorker("Reload", Runtime.getRuntime().availableProcessors()));
    
    public static void shutdownExecutors() {
        RELOADER_WORKER.ifInitialized(ThreadUtils::shutdownExecutor);
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
    
    private static ExecutorService createWorker(String name, int threads) {
        return new ForkJoinPool(threads, pool -> {
            ForkJoinWorkerThread thread = new ForkJoinWorkerThread(pool) {
                @Override
                protected void onTermination(Throwable exception) {
                    if (exception == null) {
                        LOGGER.debug("{} shutdown gracefully!", this.getName());
                    } else {
                        LOGGER.warn("{} was aborted: {}", this.getName(), exception);
                    }
                    super.onTermination(exception);
                }
            };
            thread.setName("Worker-" + name);
            return thread;
        }, (thread, throwable) -> {
            LOGGER.error("Exception caught in thread {}: {}", thread, throwable);
        }, true);
    }
}
