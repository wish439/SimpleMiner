package com.wishtoday.ts.simpleminer.crop;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@Service
public class SingleCropBlockHandler {

    private final CropBlockMatcher matcher;
    private final List<CropHandler> cropHandlers;

    @CreateConstruction
    public SingleCropBlockHandler(CropBlockMatcher matcher, List<CropHandler> cropHandlers) {
        this.matcher = matcher;
        this.cropHandlers = cropHandlers;
    }

    public boolean tryHandle(PlayerEntity player, BlockPos pos
            , BlockState state, ItemStackCollector stackCollector) {
        if (!matcher.isSupportedCrop(state.getBlock())) return false;
        BlockEntity blockEntity = state.hasBlockEntity() ? player.getWorld().getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDroppedStacks(state, (ServerWorld) player.getWorld(), pos, blockEntity, player, ItemStack.EMPTY);

        for (ItemStack stack : drops) {
            if (Block.getBlockFromItem(stack.getItem()) == state.getBlock() && !(state.getBlock() instanceof SweetBerryBushBlock)) {
                stack.decrement(1);
            }
            stackCollector.collectItemStack(stack);
        }
        this.cropHandlers.forEach(cropHandler -> cropHandler.resetAge(player.getWorld(), pos, state));
        return true;
    }
}
