package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.utils.ChunkSectionScanner;
import com.wishtoday.ts.simpleminer.core.matcher.BlockMatcher;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

@Service
public class FullChunkShape implements Shape{
    private final ChunkSectionScanner scanner;

    public FullChunkShape(ChunkSectionScanner scanner) {
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
        return result;
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
