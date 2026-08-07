package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface BlockBreakerFeature {
    default void beforeCycle(BlockBreakContext blockBreakContext) {

    }

    default void beforeBlockBreak(BlockBreakContext blockBreakContext) {

    }

    default boolean allowBreak(BlockBreakContext blockBreakContext) {
        return true;
    }

    default boolean afterBlockBreakAllowContinue(BlockBreakContext blockBreakContext) {
        return true;
    }

    default void afterCycle(BlockBreakContext blockBreakContext, List<ItemStack> droppedStacks, Object2IntOpenHashMap<ItemStackKey> droppedItemsWithCount
                            //, Object2IntOpenCustomHashMap<ItemStack> containerItemWithCount
    ) {
    }
    default boolean allowCollectItem(BlockBreakContext blockBreakContext, CollectContext collectContext) {
        return true;
    }
}
