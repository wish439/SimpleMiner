package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.mixinInterface.WorldChunkExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.util.crash.CrashCallable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin extends Chunk implements WorldChunkExtension, HeightLimitView {
    @Shadow
    @Final
    private World world;

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
}
