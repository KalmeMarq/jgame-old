package me.kalmemarq.jgame.client.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface ResourceReloader {
    CompletableFuture<Void> reload(Executor prepareExecutor, Executor applyExecutor);
}
