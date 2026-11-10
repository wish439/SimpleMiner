package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.mixinInterface.ChunkSectionTestExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkExtension;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkTestExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.crash.CrashCallable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements WorldChunkExtension, HeightLimitView, WorldChunkTestExtension {
    @Shadow
    @Final
    private World world;

    @Shadow
    protected abstract <T extends BlockEntity> void updateTicker(T blockEntity);

    @Shadow
    public abstract void addBlockEntity(BlockEntity blockEntity);

    @Shadow
    public abstract @Nullable BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType);

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Unique
    @Override
    public BlockState simpleMiner$getBlockState(int i, int j, int k) {
        if (this.world.isDebugWorld()) {
            BlockState blockState = null;
            if (j == 60) {
                blockState = Blocks.BARRIER.getDefaultState();
            }

            if (j == 70) {
                blockState = DebugChunkGenerator.getBlockState(i, k);
            }

            return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
        } else {
            try {
                int l = this.getSectionIndex(j);
                if (l >= 0 && l < this.sectionArray.length) {
                    ChunkSection chunkSection = this.sectionArray[l];
                    if (!chunkSection.isEmpty()) {
                        return chunkSection.getBlockState(i & 15, j & 15, k & 15);
                    }
                }

                return Blocks.AIR.getDefaultState();
            } catch (Throwable var8) {
                CrashReport crashReport = CrashReport.create(var8, "Getting block state");
                CrashReportSection crashReportSection = crashReport.addElement("Block being got");
                crashReportSection.add("Location", (CrashCallable<String>)(() -> CrashReportSection.createPositionString(this, i, j, k)));
                throw new CrashException(crashReport);
            }
        }
    }

    @Override
    public BlockState simpleminer$setBlockState(BlockPos pos, BlockState state, boolean moved) {
        int i = pos.getY();
        ChunkSection chunkSection = this.getSection(this.getSectionIndex(i));
        boolean bl = chunkSection.isEmpty();
        if (bl && state.isAir()) {
            return null;
        } else {
            int j = pos.getX() & 15;
            int k = i & 15;
            int l = pos.getZ() & 15;
            BlockState blockState = chunkSection.setBlockState(j, k, l, state);
            if (blockState == state) {
                return null;
            } else {
                Block block = state.getBlock();
                //((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(j, i, l, state);

                if (ChunkLightProvider.needsLightUpdate(this, pos, blockState, state)) {
                    Profiler profiler = this.world.getProfiler();
                    profiler.push("updateSkyLightSources");
                    this.chunkSkyLight.isSkyLightAccessible(this, j, i, l);
                    profiler.swap("queueCheckLight");
                    //this.world.getChunkManager().getLightingProvider().checkBlock(pos);
                    profiler.pop();
                }

                boolean bl3 = blockState.hasBlockEntity();
                if (!this.world.isClient) {
                    blockState.onStateReplaced(this.world, pos, state, moved);
                } else if (!blockState.isOf(block) && bl3) {
                    this.removeBlockEntity(pos);
                }

                if (!chunkSection.getBlockState(j, k, l).isOf(block)) {
                    return null;
                } else {
                    if (!this.world.isClient) {
                        state.onBlockAdded(this.world, pos, blockState, moved);
                    }

                    if (state.hasBlockEntity()) {
                        BlockEntity blockEntity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
                        if (blockEntity == null) {
                            blockEntity = ((BlockEntityProvider)block).createBlockEntity(pos, state);
                            if (blockEntity != null) {
                                this.addBlockEntity(blockEntity);
                            }
                        } else {
                            blockEntity.setCachedState(state);
                            this.updateTicker(blockEntity);
                        }
                    }

                    this.needsSaving = true;
                    return blockState;
                }
            }
        }
    }

    @Override
    public BlockState simpleminer$setToAir(BlockPos pos, BlockState state, BlockState current, boolean moved) {
        int i = pos.getY();
        ChunkSection chunkSection = this.getSection(this.getSectionIndex(i));
        boolean bl = chunkSection.isEmpty();
        if (bl && state.isAir()) {
            return null;
        } else {
            int j = pos.getX() & 15;
            int k = i & 15;
            int l = pos.getZ() & 15;
            BlockState blockState = ((ChunkSectionTestExtension)chunkSection).simpleminer$setBlockState(j, k, l, state, current, true);
            if (blockState == state) {
                return null;
            } else {
                Block block = state.getBlock();
                //((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(j, i, l, state);
                //((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(j, i, l, state);

                if (ChunkLightProvider.needsLightUpdate(this, pos, blockState, state)) {
                    Profiler profiler = this.world.getProfiler();
                    profiler.push("updateSkyLightSources");
                    this.chunkSkyLight.isSkyLightAccessible(this, j, i, l);
                    profiler.swap("queueCheckLight");
                    //this.world.getChunkManager().getLightingProvider().checkBlock(pos);
                    profiler.pop();
                }

                boolean bl3 = blockState.hasBlockEntity();
                if (!this.world.isClient) {
                    blockState.onStateReplaced(this.world, pos, state, moved);
                } else if (!blockState.isOf(block) && bl3) {
                    this.removeBlockEntity(pos);
                }

                if (!chunkSection.getBlockState(j, k, l).isOf(block)) {
                    return null;
                } else {
                    if (!this.world.isClient) {
                        state.onBlockAdded(this.world, pos, blockState, moved);
                    }

                    if (state.hasBlockEntity()) {
                        BlockEntity blockEntity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
                        if (blockEntity == null) {
                            blockEntity = ((BlockEntityProvider)block).createBlockEntity(pos, state);
                            if (blockEntity != null) {
                                this.addBlockEntity(blockEntity);
                            }
                        } else {
                            blockEntity.setCachedState(state);
                            this.updateTicker(blockEntity);
                        }
                    }

                    this.needsSaving = true;
                    return blockState;
                }
            }
        }
    }
}
