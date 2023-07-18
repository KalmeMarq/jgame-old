package me.kalmemarq.jgame.common;

import java.util.function.Supplier;

public class Lazy<T> {
    private final Supplier<T> supplier;
    private T value;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public synchronized T get() {
        if (this.value != null) return this.value;
        this.value = this.supplier.get();
        return this.value;
    }
}
