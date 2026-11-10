package com.wishtoday.ts.simpleminer.mixinInterface;

public interface PalettedContainerTestExtension<T> {
    T simpleminer$swap(int x, int y, int z, T value);
    T simpleminer$swap(int index, T value);
}
