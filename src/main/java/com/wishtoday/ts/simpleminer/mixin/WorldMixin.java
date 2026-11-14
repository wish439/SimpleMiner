package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.debug.NanoTimerAccumulator;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkTestExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldTestExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class WorldMixin implements WorldExtension, WorldAccess, WorldTestExtension {

    @Final
    @Shadow
    public boolean isClient;

    @Shadow
    public abstract boolean isDebugWorld();

    @Shadow
    public abstract WorldChunk getWorldChunk(BlockPos pos);

    @Shadow
    public abstract void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated);

    @Shadow
    public abstract void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags);

    @Shadow
    public abstract void updateComparators(BlockPos pos, Block block);

    @Shadow
    public abstract void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock);

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

    @Override
    public boolean simpleminer$setBlockStateWithOutLightUpdate(BlockPos pos, BlockState state, int flags) {
        int maxUpdateDepth = 512;
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        } else if (!this.isClient && this.isDebugWorld()) {
            return false;
        } else {
            WorldChunk worldChunk = this.getWorldChunk(pos);
            Block block = state.getBlock();
            BlockState blockState = ((WorldChunkTestExtension)worldChunk).simpleminer$setBlockState(pos, state, (flags & Block.MOVED) != 0);
            if (blockState == null) {
                return false;
            } else {
                BlockState blockState2 = this.getBlockState(pos);
                if (blockState2 == state) {
                    if (blockState != blockState2) {
                        this.scheduleBlockRerenderIfNeeded(pos, blockState, blockState2);
                    }

                    if ((flags & Block.NOTIFY_LISTENERS) != 0
                            && (!this.isClient || (flags & Block.NO_REDRAW) == 0)
                            && (this.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING))) {
                        this.updateListeners(pos, blockState, state, flags);
                    }

                    if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
                        this.updateNeighbors(pos, blockState.getBlock());
                        if (!this.isClient && state.hasComparatorOutput()) {
                            this.updateComparators(pos, block);
                        }
                    }

                    if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
                        int i = flags & ~(Block.SKIP_DROPS | Block.NOTIFY_NEIGHBORS);
                        blockState.prepare(this, pos, i, maxUpdateDepth - 1);
                        state.updateNeighbors(this, pos, i, maxUpdateDepth - 1);
                        state.prepare(this, pos, i, maxUpdateDepth - 1);
                    }

                    this.onBlockChanged(pos, blockState, blockState2);
                }

                return true;
            }
        }
    }

    @Override
    public boolean simpleminer$setToAir(BlockPos pos, BlockState state, BlockState current, int flags, NanoTimerAccumulator setBlock, NanoTimerAccumulator removeBlockEntity, NanoTimerAccumulator blockAdded) {
        int maxUpdateDepth = 512;
        if (this.isOutOfHeightLimit(pos)) {
            return false;
        } else if (!this.isClient && this.isDebugWorld()) {
            return false;
        } else {
            WorldChunk worldChunk = this.getWorldChunk(pos);
            Block block = state.getBlock();
            BlockState blockState = ((WorldChunkTestExtension)worldChunk).simpleminer$setToAir(pos, state, current, (flags & Block.MOVED) != 0, setBlock, removeBlockEntity, blockAdded);
            if (blockState == null) {
                return false;
            } else {
                BlockState blockState2 = this.getBlockState(pos);
                if (blockState2 == state) {
                    if (blockState != blockState2) {
                        this.scheduleBlockRerenderIfNeeded(pos, blockState, blockState2);
                    }

                    if ((flags & Block.NOTIFY_LISTENERS) != 0
                            && (!this.isClient || (flags & Block.NO_REDRAW) == 0)
                            && (this.isClient || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING))) {
                        this.updateListeners(pos, blockState, state, flags);
                    }

                    if ((flags & Block.NOTIFY_NEIGHBORS) != 0) {
                        this.updateNeighbors(pos, blockState.getBlock());
                        if (!this.isClient && state.hasComparatorOutput()) {
                            this.updateComparators(pos, block);
                        }
                    }

                    if ((flags & Block.FORCE_STATE) == 0 && maxUpdateDepth > 0) {
                        int i = flags & ~(Block.SKIP_DROPS | Block.NOTIFY_NEIGHBORS);
                        blockState.prepare(this, pos, i, maxUpdateDepth - 1);
                        state.updateNeighbors(this, pos, i, maxUpdateDepth - 1);
                        state.prepare(this, pos, i, maxUpdateDepth - 1);
                    }

                    this.onBlockChanged(pos, blockState, blockState2);
                }

                return true;
            }
        }
    }
}
