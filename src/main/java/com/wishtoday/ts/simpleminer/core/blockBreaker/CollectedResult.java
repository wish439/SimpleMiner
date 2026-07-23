package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectedResult {
    private final Object2IntOpenHashMap<ItemStackKey> map;
}
