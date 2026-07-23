package com.wishtoday.ts.simpleminer.core;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public record BlockStorage(BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable NbtCompound nbtComponent) {
}
