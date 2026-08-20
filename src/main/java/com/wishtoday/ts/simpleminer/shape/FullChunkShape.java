package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.core.matcher.BlockMatcher;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

@Service
public class FullChunkShape implements Shape {
    private final SurroundChunkScanner surroundChunkScanner;
    private final ChunkSectionScanner scanner;

    @CreateConstruction
    public FullChunkShape(SurroundChunkScanner surroundChunkScanner, ChunkSectionScanner scanner) {
        this.surroundChunkScanner = surroundChunkScanner;
        this.scanner = scanner;
    }

    @Override
    public LongOpenHashSet walk(ShapeContext context) {
        BlockPos pos = context.getCurrentTargetPos();
        World world = context.getWorld();
        Chunk chunk = world.getChunk(pos);
        ChunkSection[] array = chunk.getSectionArray();
        BlockState state = context.getCurrentTargetState();
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int bottomY = world.getBottomY();
        LongOpenHashSet result = new LongOpenHashSet();
        BlockMatcher matcher = context.getMatcher();
        result.add(pos.asLong());

        handleChunk(context, pos, bottomY, array, matcher, state, startX, startZ, result);

        IndividualConfig config = context.getIndividualConfig();
        surroundChunkScanner.forEachSurroundChunk(c -> {
                    ChunkPos cPos = c.getPos();
                    if (cPos.equals(chunk.getPos())) {
                        return;
                    }
                    if (result.size() >= context.getMaxSize()) return;
                    handleChunk(context, pos, bottomY, c.getSectionArray(), matcher, state, cPos.getStartX(), cPos.getStartZ(), result);
                }
                , config.getFullChunkShapeInfos().getRadiusX()
                , config.getFullChunkShapeInfos().getRadiusZ()
                , chunk, world);
        return result;
    }

    private void handleChunk(ShapeContext context, BlockPos pos, int bottomY, ChunkSection[] array, BlockMatcher matcher, BlockState state, int startX, int startZ, LongOpenHashSet result) {
        int playerSectionIndex = (pos.getY() - bottomY) / 16;
        int totalSections = array.length;

        for (int i = playerSectionIndex; i >= 0; i--) {
            ChunkSection chunkSection = array[i];
            int baseY = bottomY + i * 16;
            this.scanner.matchSection(chunkSection, matcher::match, state, startX, baseY, startZ, context.getMaxSize(), result);
        }
        for (int i = playerSectionIndex + 1; i < totalSections; i++) {
            ChunkSection chunkSection = array[i];
            int baseY = bottomY + i * 16;
            this.scanner.matchSection(chunkSection, matcher::match, state, startX, baseY, startZ, context.getMaxSize(), result);
        }
    }

    @Override
    public int index() {
        return 2;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("shape.full_chunk");
    }
}
