package com.wishtoday.ts.simpleminer.undo;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import com.wishtoday.ts.simpleminer.io.PersistenceService;
import com.wishtoday.ts.simpleminer.utils.ItemStackUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UndoConductor {
    private final PressManager pressManager;
    private final PersistenceService persistence;

    @CreateConstruction
    public UndoConductor(PressManager pressManager, PersistenceService persistence) {
        this.pressManager = pressManager;
        this.persistence = persistence;
    }

    @Nullable
    public UndoStorage getUndoStorage(PlayerEntity player, UUID uuid) {
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        return info.getUndoHistory().getUndoStorage(uuid);
    }

    public boolean returnAllMaterial(PlayerEntity player, UUID uuid) {
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        UndoStorage undoStorage = info.getUndoHistory().getUndoStorage(uuid);
        if (undoStorage == null) return false;
        Map<ItemStackKey, MaterialInfo> map = undoStorage.getItems();
        return this.returnAllMaterial(map, player);
    }

    public boolean returnAllMaterial(Map<ItemStackKey, MaterialInfo> map, PlayerEntity player) {
        Object2IntOpenHashMap<ItemStackKey> hashMap = new Object2IntOpenHashMap<>();
        map.forEach((key, stack) -> hashMap.put(key, stack.getCurrentCount()));
        List<ItemStack> itemStacks = ItemStackUtils.spiltDroppedStacks(hashMap);
        itemStacks.forEach(a -> this.giveOrDrop(player, a));
        return !itemStacks.isEmpty();
    }

    private void giveOrDrop(PlayerEntity player, ItemStack itemStack) {
        boolean b = player.giveItemStack(itemStack);
        if (!b) {
            ItemEntity itemEntity = player.dropItem(itemStack, true);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
            }
        }
    }

    public void undo(PlayerEntity player, UUID uuid) {
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        UndoStorage undoStorage = info.getUndoHistory().getUndoStorage(uuid);
        if (undoStorage != null) {
            this.performUndo(player, info, undoStorage, uuid);
            return;
        }
        // 不在内存(已被淘汰到磁盘):异步加载后回到主线程执行
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        this.persistence.loadUndoAsync(serverPlayer.getUuid(), uuid, (loaded, error) -> {
            if (loaded == null) return;
            this.performUndo(serverPlayer, info, loaded, uuid);
        });
    }

    private void performUndo(PlayerEntity player, PlayerMinerInfo info, UndoStorage undoStorage, UUID uuid) {
        this.undo(player.getWorld(), undoStorage);
        info.getUndoHistory().removeUndoStorage(uuid);
        this.persistence.deleteUndo(player.getUuid(), uuid);
    }

    private void undo(World world, UndoStorage undoStorage) {
        Long2ObjectLinkedOpenHashMap<BlockStorage> map = undoStorage.getMap();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Long2ObjectMap.Entry<BlockStorage> blockStorageEntry : map.long2ObjectEntrySet()) {
            BlockStorage blockStorage = blockStorageEntry.getValue();
            long longKey = blockStorageEntry.getLongKey();
            mutable.set(longKey);
            world.setBlockState(mutable, blockStorage.blockState());
            NbtCompound nbtCompound = blockStorage.nbtComponent();
            if (nbtCompound == null) continue;
            BlockEntity blockEntity = blockStorage.blockEntity();
            if (blockEntity == null) {
                // 从磁盘加载的记录只有 NBT,从世界中取新建的方块实体
                blockEntity = world.getBlockEntity(mutable);
            }
            if (blockEntity == null) continue;
            blockEntity.read(nbtCompound, world.getRegistryManager());
            world.addBlockEntity(blockEntity);
        }
    }
}
