package me.kalmemarq.jgame.common;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public abstract class ThreadExecutor implements Executor {
    private final Queue<Runnable> runnables = new ConcurrentLinkedQueue<>();

    @Override
    public void execute(@NotNull Runnable command) {
        if (Thread.currentThread() == this.getMainThread()) {
            command.run();
        } else {
            this.runnables.add(command);
        }
    }
    
    public abstract Thread getMainThread();

    public void runQueueTask() {
        if (this.runnables.size() == 0) return;
        this.runnables.poll().run();
    }
    
    public void runTask() {
        if (this.runnables.size() == 0) return;
        this.runnables.poll().run();
    }

    public void runTasks() {
        while (this.runnables.size() > 0) {
            this.runnables.poll().run();
        }
    }
}
