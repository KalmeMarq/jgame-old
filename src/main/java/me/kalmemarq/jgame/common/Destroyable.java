package me.kalmemarq.jgame.common;

public interface Destroyable {
    /**
     * Releases any underlying resources. It should always be implicitly called.
     * If memory management can be done, it should be by the developer.
     */
    void destroy();
}
