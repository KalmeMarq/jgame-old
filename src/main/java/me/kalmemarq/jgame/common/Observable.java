package me.kalmemarq.jgame.common;

import java.util.function.Consumer;

public interface Observable<T> {
    Observable<T> subscribe(Consumer<T> listener);
    void unsubscribe(Consumer<T> listener);
    void notifyAllSubscribers();
}
