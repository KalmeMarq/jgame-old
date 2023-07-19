package me.kalmemarq.jgame.client.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class PreparationResourceReloader<T> implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(this::prepare, prepareExecutor).thenAcceptAsync(this::apply, applyExecutor);
    }

    protected abstract T prepare();

    protected abstract void apply(T prepared);
}
