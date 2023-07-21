package me.kalmemarq.jgame.client.resource;

import me.kalmemarq.jgame.common.Unit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SyncResourceReloader implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(ResourceLoader.PreparationSyncer preparationSyncer, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> Unit.UNIT, prepareExecutor).thenComposeAsync(preparationSyncer::onComplete).thenAcceptAsync((u) -> this.reload(), applyExecutor);
    }

    protected abstract void reload();
}
