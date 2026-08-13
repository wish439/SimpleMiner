package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.ChunkSectionScanner;
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
        LongOpenHashSet longs = new LongOpenHashSet();
        BlockMatcher matcher = context.getMatcher();
        longs.add(pos.asLong());
        for (int i = 0; i < array.length; i++) {
            ChunkSection chunkSection = array[i];
            int y = bottomY + 16 * i;
            this.scanner.matchSection(chunkSection, matcher::match, state, startX, y, startZ, context.getMaxSize(), longs);
        }
        return longs;
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
