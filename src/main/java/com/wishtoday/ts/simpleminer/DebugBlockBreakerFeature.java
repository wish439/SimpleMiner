package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakerFeature;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

//@Service
public class DebugBlockBreakerFeature implements BlockBreakerFeature {
    private long startTime;
    @Override
    public void beforeCycle(BlockBreakContext blockBreakContext) {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void afterCycle(BlockBreakContext blockBreakContext, List<ItemStack> droppedStacks, Object2IntOpenHashMap<ItemStackKey> droppedItemsWithCount) {
        PlayerEntity player = blockBreakContext.getPlayer();
        player.sendMessage(Text.of("本次总连锁" + blockBreakContext.getShapeResult().getSortedBlockPoses().size() + "方块，耗时" + (System.currentTimeMillis() - this.startTime) + "ms"));
    }
}
