package com.wishtoday.ts.simpleminer.registry;

public interface Registry<T> {
    boolean register(T t);
    default void onRegistered(T t) {}
    Class<T> handleClass();
}
