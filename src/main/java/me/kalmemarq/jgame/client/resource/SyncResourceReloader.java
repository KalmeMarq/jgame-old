package me.kalmemarq.jgame.client.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SyncResourceReloader implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(this::reload, applyExecutor);
    }

    protected abstract void reload();
}
