package me.kalmemarq.jgame.client.resource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceLoader {
    private final AtomicInteger prepared;
    private final AtomicInteger applied;
    private int total;
    private boolean isActive;

    public ResourceLoader() {
        this.prepared = new AtomicInteger();
        this.applied = new AtomicInteger();
        this.total = 0;
    }

    public void start(List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, ResourceManager resourceManager, Runnable onComplete) {
        this.prepared.set(0);
        this.applied.set(0);
        this.total = reloaders.size();
        this.isActive = true;

        CompletableFuture<?>[] futures = new CompletableFuture[reloaders.size()];

        for (int i = 0; i < this.total; i++) {
            futures[i] = reloaders.get(i).reload(new PreparationSyncer() {
                @Override
                public <T> CompletableFuture<T> onPreparationComplete(T v) {
                    ResourceLoader.this.prepared.incrementAndGet();
                    return CompletableFuture.supplyAsync(() -> v);
                }
            }, prepareExecutor, applyExecutor, resourceManager).whenComplete((_v0, _v1) -> {
                this.applied.incrementAndGet();
            });
        }

        CompletableFuture.allOf(futures).whenComplete((_v0, _v1) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                this.isActive = false;
                onComplete.run();
            });
        });
    }

    public float getProgress() {
        return (float)(this.prepared.get() + this.applied.get()) / (this.total * 2f);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public interface PreparationSyncer {
        <T> CompletableFuture<T> onPreparationComplete(T v);
    }
}
