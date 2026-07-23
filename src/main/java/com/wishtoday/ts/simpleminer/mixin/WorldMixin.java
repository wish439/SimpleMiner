package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin implements WorldExtension, WorldAccess {

    @Override
    public BlockState simpleMiner$getBlockState(int x, int y, int z) {
        if (this.isOutOfHeightLimit(y)) {
            return Blocks.VOID_AIR.getDefaultState();
        } else {
            WorldChunk worldChunk = (WorldChunk) this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
            return ((WorldChunkExtension)worldChunk).simpleMiner$getBlockState(x, y, z);
        }
    }

    @Override
    public BlockState simpleMiner$getBlockState(long pos) {
        return this.simpleMiner$getBlockState(BlockPos.unpackLongX(pos), BlockPos.unpackLongY(pos), BlockPos.unpackLongZ(pos));
    }
}
