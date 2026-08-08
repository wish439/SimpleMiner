package com.wishtoday.ts.simpleminer.undo;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class UndoStorage {
    private final Long2ObjectLinkedOpenHashMap<BlockStorage> map;
    private Map<ItemStackKey, MaterialInfo> items;
    private int completedCount;
    private final long time;
    private final UUID uuid;

    public UndoStorage(Long2ObjectLinkedOpenHashMap<BlockStorage> map, Map<ItemStackKey, MaterialInfo> items, long time) {
        this.map = map;
        this.items = items;
        this.time = time;
        this.completedCount = 0;
        this.uuid = UUID.randomUUID();
    }
}
