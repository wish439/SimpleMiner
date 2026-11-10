package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.mixinInterface.ChunkSectionTestExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.PalettedContainerTestExtension;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements ChunkSectionTestExtension {
    @Shadow
    @Final
    private PalettedContainer<BlockState> blockStateContainer;

    @Shadow
    private short nonEmptyBlockCount;

    @Shadow
    private short randomTickableBlockCount;

    @Shadow
    private short nonEmptyFluidCount;

    @Override
    public BlockState simpleminer$setBlockState(int x, int y, int z, BlockState state, BlockState current, boolean lock) {
        if (lock) {
            ((PalettedContainerTestExtension<BlockState>)(this.blockStateContainer)).simpleminer$swap(x, y, z, state);
        } else {
            this.blockStateContainer.swapUnsafe(x, y, z, state);
        }

        FluidState fluidState = current.getFluidState();
        FluidState fluidState2 = state.getFluidState();
        if (!current.isAir()) {
            this.nonEmptyBlockCount--;
            if (current.hasRandomTicks()) {
                this.randomTickableBlockCount--;
            }
        }

        if (!fluidState.isEmpty()) {
            this.nonEmptyFluidCount--;
        }

        if (!state.isAir()) {
            this.nonEmptyBlockCount++;
            if (state.hasRandomTicks()) {
                this.randomTickableBlockCount++;
            }
        }

        if (!fluidState2.isEmpty()) {
            this.nonEmptyFluidCount++;
        }

        return current;
    }
}
