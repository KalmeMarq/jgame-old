package me.kalmemarq.jgame.client.resource;

import me.kalmemarq.jgame.common.Unit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class SyncResourceReloader implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(ResourceLoader.PreparationSyncer preparationSyncer, Executor prepareExecutor, Executor applyExecutor, ResourceManager resourceManager) {
        return preparationSyncer.onPreparationComplete(Unit.INSTANCE).thenRunAsync(() -> this.reload(resourceManager), applyExecutor);
    }

    protected abstract void reload(ResourceManager resourceManager);
}
