package com.wishtoday.ts.simpleminer.crop;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.mixin.Accessor.IntPropertyAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Service
public class UsedAgeCropHandler implements CropHandler {
    private final CropBlockMatcher matcher;

    @CreateConstruction
    public UsedAgeCropHandler(CropBlockMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public void resetAge(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (!matcher.isSupportedCrop(block)) {
            return;
        }
        StateManager<Block, BlockState> manager = block.getStateManager();
        Property<?> age = manager.getProperty("age");
        if (age instanceof IntProperty i) {
            Integer integer = state.get(i);
            IntPropertyAccessor accessor = (IntPropertyAccessor) i;
            if (accessor.getMax() > integer) {
                return;
            }
            BlockState with = state.with(i, accessor.getMin());
            world.setBlockState(pos, with, Block.NOTIFY_ALL);
        }
    }
}
