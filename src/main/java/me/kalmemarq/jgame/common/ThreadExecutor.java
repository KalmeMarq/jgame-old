package me.kalmemarq.jgame.common;

import me.kalmemarq.jgame.common.logger.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;

public abstract class ThreadExecutor<R extends Runnable> implements Executor {
    private static final Logger LOGGER = Logger.getLogger();
    private final Queue<R> tasks = new ConcurrentLinkedQueue<>();

    public boolean isOnRequiredThread() {
        return Thread.currentThread() == this.getRequiredThread();
    }
    
    abstract public R createTask(Runnable runnable);
    
    public void send(R runnable) {
        this.tasks.add(runnable);
        LockSupport.unpark(this.getRequiredThread());
    }
    
    @Override
    public void execute(@NotNull Runnable command) {
        if (this.isOnRequiredThread()) {
            command.run();
        } else {
            this.send(this.createTask(command));
        }
    }
    
    public abstract Thread getRequiredThread();
    
    public void runTask() {
        if (this.tasks.isEmpty()) return;
        this.tasks.poll().run();
    }

    public void runTasks() {
        while (!this.tasks.isEmpty()) {
            this.tasks.poll().run();
        }
    }
    
    private void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("", 100000L);
    }
    
    public void cancelAllTasks() {
        this.tasks.clear();
    }
}
