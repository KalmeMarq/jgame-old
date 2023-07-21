package me.kalmemarq.jgame.client.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class PreparationResourceReloader<T> implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(ResourceLoader.PreparationSyncer preparationSyncer, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(this::prepare, prepareExecutor).thenComposeAsync(preparationSyncer::onComplete).thenAcceptAsync(this::apply, applyExecutor);
    }

    protected abstract T prepare();

    protected abstract void apply(T prepared);
}
