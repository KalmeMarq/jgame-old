package me.kalmemarq.jgame.common;

import java.nio.file.Path;

public abstract class Server extends ThreadExecutor<Runnable> {
    abstract public boolean isDedicated();
    
    public Path getRunPath() {
        return Path.of(".");
    }
    
    public void tick() {
    }

    @Override
    public Runnable createTask(Runnable runnable) {
        return runnable;
    }
}
