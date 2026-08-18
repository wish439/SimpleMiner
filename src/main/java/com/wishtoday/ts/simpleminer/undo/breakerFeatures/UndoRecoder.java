package com.wishtoday.ts.simpleminer.undo.breakerFeatures;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakerFeature;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import com.wishtoday.ts.simpleminer.mixinInterface.WorldExtension;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UndoRecoder implements BlockBreakerFeature {

    private final Long2ObjectLinkedOpenHashMap<BlockStorage> map;
    private final ServerConfig config;

    @CreateConstruction
    public UndoRecoder(ServerConfig config) {
        this.config = config;
        this.map = new Long2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public boolean allowBreak(BlockBreakContext blockBreakContext) {
        if (!config.isAllowUndo()) return true;
        map.put(blockBreakContext.getCurrentBlockPos(), this.getBlockStorage(blockBreakContext.getWorld(), blockBreakContext.getCurrentPos(), blockBreakContext.getCurrentState()));
        return true;
    }

    @Override
    public void afterCycle(BlockBreakContext blockBreakContext, List<ItemStack> droppedStacks, Object2IntOpenHashMap<ItemStackKey> droppedItemsWithCount) {
        if (!config.isAllowUndo()) return;
        Long2ObjectLinkedOpenHashMap<BlockStorage> copy = new Long2ObjectLinkedOpenHashMap<>(this.map);
        this.makeUndoForPlayer(blockBreakContext.getInfo(), copy, droppedItemsWithCount);
        this.map.clear();
    }

    private BlockStorage getBlockStorage(World world, BlockPos pos, BlockState blockState) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        NbtCompound nbtComponent = null;
        if (blockEntity != null) nbtComponent = blockEntity.createNbt(world.getRegistryManager());
        return new BlockStorage(blockState, blockEntity, nbtComponent);
    }

    private void makeUndoForPlayer(PlayerMinerInfo playerMinerInfo, Long2ObjectLinkedOpenHashMap<BlockStorage> blockPoses, Object2IntOpenHashMap<ItemStackKey> itemStacks) {
        HashMap<ItemStackKey, MaterialInfo> newMap = new HashMap<>();
        for (Object2IntMap.Entry<ItemStackKey> entry : itemStacks.object2IntEntrySet()) {
            int intValue = entry.getIntValue();
            ItemStackKey key = entry.getKey();
            MaterialInfo info = new MaterialInfo(key.itemStack(), intValue, 0);
            newMap.put(key, info);
        }
        playerMinerInfo.getUndoHistory().addUndoStorage(new UndoStorage(blockPoses, newMap, System.currentTimeMillis()));
    }
}
