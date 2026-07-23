package com.wishtoday.ts.simpleminer.core.blockBreaker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectContext {
    private World world;
    @Nullable
    private PlayerEntity player;
    @Nullable
    private BlockPos pos;
    @Nullable
    private ItemStack handStack;

    @Nullable
    private Entity itemEntity;
}
