package me.kalmemarq.jgame.common;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a value which is initialized lazily.
 */
public final class Lazy<T> {
    private Supplier<T> initializer;
    private volatile T value = null;

    public Lazy(Supplier<T> initializer) {
        this.initializer = initializer;
    }

    /**
     * Gets the lazily initialized value. It's thread safe.
     */
    public T get() {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    this.value = this.initializer.get();
                    this.initializer = null;
                }
            }
        }
        return this.value;
    }

    /**
     * Returns {@code true} if the value has already been initialized, otherwise {@code false}.
     */
    public boolean isInitialized() {
        return this.value != null;
    }

    /**
     * If the value has already been initialized, it performs the given action with the value,
     * otherwise does nothing.
     */
    public void ifInitialized(Consumer<T> consumer) {
        if (this.isInitialized()) consumer.accept(this.value);
    }
}
