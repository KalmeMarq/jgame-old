package me.kalmemarq.jgame.common;

public interface Destroyable extends AutoCloseable {
    void destroy();

    @Override
    default void close() throws Exception {
        this.destroy();
    }
}
