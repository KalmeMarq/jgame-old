package me.kalmemarq.jgame.client.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class PreparationResourceReloader<T> implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(ResourceLoader.PreparationSyncer preparationSyncer, Executor prepareExecutor, Executor applyExecutor, ResourceManager resourceManager) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager), prepareExecutor).thenComposeAsync(preparationSyncer::onPreparationComplete).thenAcceptAsync((o) -> this.apply(o, resourceManager), applyExecutor);
    }

    protected abstract T prepare(ResourceManager resourceManager);

    protected abstract void apply(T prepared, ResourceManager resourceManager);
}
