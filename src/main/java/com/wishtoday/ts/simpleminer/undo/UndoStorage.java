package com.wishtoday.ts.simpleminer.undo;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import lombok.Data;

import java.util.Map;

@Data
public class UndoStorage {
    private final Long2ObjectLinkedOpenHashMap<BlockStorage> map;
    private Map<ItemStackKey, MaterialInfo> items;
    private int completedCount;
    private final long time;

    public UndoStorage(Long2ObjectLinkedOpenHashMap<BlockStorage> map, Map<ItemStackKey, MaterialInfo> items, long time) {
        this.map = map;
        this.items = items;
        this.time = time;
        this.completedCount = 0;
    }
}
