package com.wishtoday.ts.simpleminer.utils;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.mixin.Accessor.PalettedContainerAccessor;
import com.wishtoday.ts.simpleminer.mixin.Accessor.PalettedContainerPaletteProviderAccessor;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.BitSet;
import java.util.function.BiPredicate;

@Service
public class ChunkSectionScanner {
    public LongOpenHashSet matchSection(ChunkSection section
            , BiPredicate<BlockState, BlockState> predicate
            , BlockState matchState
            , int baseX, int baseY, int baseZ, int maxSize, LongOpenHashSet result) {
        if (section.isEmpty()) {
            return result;
        }
        if (matchState.isAir()) return result;
        PalettedContainer<BlockState> container = section.getBlockStateContainer();
        @SuppressWarnings("unchecked")
        PalettedContainerAccessor<BlockState> accessor = (PalettedContainerAccessor<BlockState>) container;
        PalettedContainer.Data<BlockState> data = accessor.getData();
        PaletteStorage storage = data.storage();
        Palette<BlockState> palette = data.palette();
        /*int indexed = palette.index(matchState);
        if (indexed < 0) {
            return result;
        }*/
        int elementBits = storage.getElementBits();
        if (elementBits == 0) {
            BlockState state = palette.get(0);
            if (predicate.test(matchState, state)) {
                this.addAllToResult(result, baseX, baseY, baseZ, maxSize);
            }
        }
        long[] longs = storage.getData();

        BitSet set = this.builtAllowedBlockBiteSet(palette, matchState, predicate);

        int elementsPerLong = (char) (64 / elementBits);
        long maxValue = (1L << elementBits) - 1L;
        long value;
        int size = storage.getSize();
        int index = 0;
        for (long aLong : longs) {
            value = aLong;
            int a = Math.min(elementsPerLong, size - index);
            for (int i = 0; i < a; i++) {
                if (result.size() >= maxSize) {
                    return result;
                }
                long l = value & maxValue;
                if (set.get((int) l)) {
                    long relativePosFromIndex = this.computeRelativePosFromIndex(index, container);
                    int x = baseX + BlockPos.unpackLongX(relativePosFromIndex);
                    int y = baseY + BlockPos.unpackLongY(relativePosFromIndex);
                    int z = baseZ + BlockPos.unpackLongZ(relativePosFromIndex);
                    result.add(BlockPos.asLong(x, y, z));
                }
                value >>= elementBits;
                index++;
            }
        }
        return result;
    }

    private void addAllToResult(LongOpenHashSet set, int baseX
            , int baseY, int baseZ, int maxSize) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    if (set.size() >= maxSize) {
                        return;
                    }
                    set.add(BlockPos.asLong(baseX + i, baseY + j, baseZ + k));
                }
            }
        }
    }

    private BitSet builtAllowedBlockBiteSet(Palette<BlockState> palette
            , BlockState original
            , BiPredicate<BlockState, BlockState> predicate) {
        BitSet set = new BitSet();
        int size = palette.getSize();
        for (int i = 0; i < size; i++) {
            BlockState state = palette.get(i);
            if (predicate.test(state, original)) {
                set.set(i);
            }
        }
        return set;
    }

    private long computeRelativePosFromIndex(int index
            , PalettedContainer<BlockState> container) {
        PalettedContainerAccessor<BlockState> containerAccessor = (PalettedContainerAccessor<BlockState>) container;
        PalettedContainer.PaletteProvider paletteProvider = containerAccessor.getPaletteProvider();
        PalettedContainerPaletteProviderAccessor providerAccessor = (PalettedContainerPaletteProviderAccessor) paletteProvider;
        int edgeBits = providerAccessor.getEdgeBits();
        int i = (1 << edgeBits) - 1;
        int x = index & i;
        int z = (index >> edgeBits) & i;
        int y = index >> (2 * edgeBits);
        return BlockPos.asLong(x, y, z);
    }
}