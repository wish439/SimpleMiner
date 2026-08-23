package com.wishtoday.ts.simpleminer.io;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UndoStorage <-> NBT(.dat) 编解码。
 * 方块状态/方块实体 NBT 用原版 CODEC 序列化,存储格式与 MC 的 .dat 习惯一致。
 */
public final class UndoStorageCodec {
    private static final String KEY_UUID = "Uuid";
    private static final String KEY_TIME = "Time";
    private static final String KEY_COMPLETED = "CompletedCount";
    private static final String KEY_BLOCKS = "Blocks";
    private static final String KEY_POS = "Pos";
    private static final String KEY_STATE = "State";
    private static final String KEY_BLOCK_ENTITY = "BlockEntity";
    private static final String KEY_ITEMS = "Items";
    private static final String KEY_STACK = "Stack";
    private static final String KEY_MAX_COUNT = "MaxCount";
    private static final String KEY_CURRENT_COUNT = "CurrentCount";

    private UndoStorageCodec() {
    }

    public static NbtCompound encode(UndoStorage storage, RegistryWrapper.WrapperLookup registryManager) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);
        NbtCompound root = new NbtCompound();
        root.putUuid(KEY_UUID, storage.getUuid());
        root.putLong(KEY_TIME, storage.getTime());
        root.putInt(KEY_COMPLETED, storage.getCompletedCount());

        NbtList blocks = new NbtList();
        storage.getMap().long2ObjectEntrySet().forEach(entry -> {
            NbtCompound tag = new NbtCompound();
            tag.putLong(KEY_POS, entry.getLongKey());
            BlockStorage blockStorage = entry.getValue();
            tag.put(KEY_STATE, encodeBlockState(blockStorage.blockState(), ops));
            if (blockStorage.nbtComponent() != null) {
                tag.put(KEY_BLOCK_ENTITY, blockStorage.nbtComponent());
            }
            blocks.add(tag);
        });
        root.put(KEY_BLOCKS, blocks);

        NbtList items = new NbtList();
        storage.getItems().forEach((key, info) -> {
            NbtCompound tag = new NbtCompound();
            tag.put(KEY_STACK, encodeItemStack(key.itemStack(), ops));
            tag.putInt(KEY_MAX_COUNT, info.getMaxCount());
            tag.putInt(KEY_CURRENT_COUNT, info.getCurrentCount());
            items.add(tag);
        });
        root.put(KEY_ITEMS, items);
        return root;
    }

    public static UndoStorage decode(NbtCompound root, RegistryWrapper.WrapperLookup registryManager) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);

        Long2ObjectLinkedOpenHashMap<BlockStorage> blocks = new Long2ObjectLinkedOpenHashMap<>();
        NbtList blocksTag = root.getList(KEY_BLOCKS, NbtElement.COMPOUND_TYPE);
        for (NbtElement element : blocksTag) {
            NbtCompound tag = (NbtCompound) element;
            long pos = tag.getLong(KEY_POS);
            BlockState state = decodeBlockState(tag.get(KEY_STATE), ops);
            NbtCompound blockEntityNbt = tag.contains(KEY_BLOCK_ENTITY) ? tag.getCompound(KEY_BLOCK_ENTITY) : null;
            blocks.put(pos, new BlockStorage(state, null, blockEntityNbt));
        }

        Map<ItemStackKey, MaterialInfo> items = new HashMap<>();
        NbtList itemsTag = root.getList(KEY_ITEMS, NbtElement.COMPOUND_TYPE);
        for (NbtElement element : itemsTag) {
            NbtCompound tag = (NbtCompound) element;
            ItemStack stack = decodeItemStack(tag.get(KEY_STACK), ops);
            int maxCount = tag.getInt(KEY_MAX_COUNT);
            int currentCount = tag.getInt(KEY_CURRENT_COUNT);
            items.put(new ItemStackKey(stack), new MaterialInfo(stack, maxCount, currentCount));
        }

        return new UndoStorage(blocks, items, root.getInt(KEY_COMPLETED), root.getLong(KEY_TIME), root.getUuid(KEY_UUID));
    }

    /**
     * 只解码列表 GUI 需要的元信息(time/物品摘要),跳过 blocks,供磁盘记录异步加载使用。
     */
    public static UndoDisplayInfo decodeMeta(NbtCompound root, RegistryWrapper.WrapperLookup registryManager) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);
        Map<ItemStackKey, MaterialInfo> items = new HashMap<>();
        NbtList itemsTag = root.getList(KEY_ITEMS, NbtElement.COMPOUND_TYPE);
        for (NbtElement element : itemsTag) {
            NbtCompound tag = (NbtCompound) element;
            ItemStack stack = decodeItemStack(tag.get(KEY_STACK), ops);
            int maxCount = tag.getInt(KEY_MAX_COUNT);
            int currentCount = tag.getInt(KEY_CURRENT_COUNT);
            items.put(new ItemStackKey(stack), new MaterialInfo(stack, maxCount, currentCount));
        }
        List<ItemStack> stacks = items.values().stream()
                .sorted(Comparator.comparingInt(MaterialInfo::getMaxCount).reversed())
                .limit(3)
                .map(info -> {
                    ItemStack stack = info.getItemStack();
                    stack.setCount(1);
                    return stack;
                })
                .toList();
        String text = firstPosText(root);
        return new UndoDisplayInfo(text, root.getLong(KEY_TIME), root.getUuid(KEY_UUID), stacks, items.size() > 3);
    }

    /** 从 blocks 列表第一个条目取坐标作为列表显示文本 */
    private static String firstPosText(NbtCompound root) {
        NbtList blocksTag = root.getList(KEY_BLOCKS, NbtElement.COMPOUND_TYPE);
        if (blocksTag.isEmpty()) {
            return "?";
        }
        long pos = ((NbtCompound) blocksTag.get(0)).getLong(KEY_POS);
        int x = BlockPos.unpackLongX(pos);
        int y = BlockPos.unpackLongY(pos);
        int z = BlockPos.unpackLongZ(pos);
        return x + "," + y + "," + z;
    }

    private static NbtElement encodeBlockState(BlockState state, RegistryOps<NbtElement> ops) {
        return BlockState.CODEC.encodeStart(ops, state).result().orElseThrow();
    }

    private static BlockState decodeBlockState(NbtElement element, RegistryOps<NbtElement> ops) {
        return BlockState.CODEC.parse(ops, element).result().orElseThrow();
    }

    private static NbtElement encodeItemStack(ItemStack stack, RegistryOps<NbtElement> ops) {
        return ItemStack.CODEC.encodeStart(ops, stack).result().orElseThrow();
    }

    private static ItemStack decodeItemStack(NbtElement element, RegistryOps<NbtElement> ops) {
        return ItemStack.CODEC.parse(ops, element).result().orElseThrow();
    }
}
