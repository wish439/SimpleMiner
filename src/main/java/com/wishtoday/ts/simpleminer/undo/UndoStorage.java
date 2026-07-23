package com.wishtoday.ts.simpleminer.undo;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.core.BlockStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
public class UndoStorage {
    private final Long2ObjectLinkedOpenHashMap<BlockStorage> map;
    private Map<ItemStackKey, MaterialInfo> items;
    private int completedCount;

    public UndoStorage(Long2ObjectLinkedOpenHashMap<BlockStorage> map, Map<ItemStackKey, MaterialInfo> items) {
        this.map = map;
        this.items = items;
        this.completedCount = 0;
    }
}
