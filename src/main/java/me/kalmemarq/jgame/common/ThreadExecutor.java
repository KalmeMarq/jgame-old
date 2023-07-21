package me.kalmemarq.jgame.common;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executor;

public class ThreadExecutor implements Executor {
    private Deque<Runnable> runnables = new ArrayDeque<>();

    @Override
    public void execute(@NotNull Runnable command) {
        this.runnables.add(command);
    }

    public void runQueueTask() {
        if (this.runnables.size() == 0) return;
        this.runnables.pollFirst().run();
    }
}
