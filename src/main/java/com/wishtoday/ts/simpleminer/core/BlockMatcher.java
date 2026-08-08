package com.wishtoday.ts.simpleminer.core;

import com.wishtoday.ts.simpleminer.mixin.Accessor.RegistryEntryReferenceAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockMatcher {
    private final Map<Block, IntOpenHashSet> blockRawIds;
    private final Set<TagKey<Block>> allowedTags;
    private final Map<Block, Set<TagKey<Block>>> tagCache;

    public BlockMatcher() {
        this.blockRawIds = new HashMap<>();
        this.allowedTags = new HashSet<>();
        this.tagCache = new HashMap<>();
    }

    public boolean match(Block a, Block b) {
        if (a == b) {
            return true;
        }
        IntOpenHashSet ints = this.blockRawIds.get(a);
        if (fromIdTryMatch(a, b, ints)) {
            return true;
        }
        Set<TagKey<Block>> aTags = getOrCreateTags(a);
        Set<TagKey<Block>> bTags = getOrCreateTags(b);
        for (TagKey<Block> tag : aTags) {
            if (bTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private Set<TagKey<Block>> getOrCreateTags(Block a) {
        Set<TagKey<Block>> aTags;
        if (tagCache.containsKey(a)) {
            aTags = tagCache.get(a);
        } else {
            aTags = this.getFilteredTags(a);
            this.tagCache.put(a, aTags);
        }
        return aTags;
    }

    private @NotNull Set<TagKey<Block>> getFilteredTags(Block a) {
        RegistryEntry.Reference<Block> entry = a.getRegistryEntry();
        RegistryEntryReferenceAccessor<Block> accessor = (RegistryEntryReferenceAccessor<Block>) entry;
        return accessor.getTags()
                .stream().filter(allowedTags::contains)
                .collect(Collectors.toSet());
    }

    private boolean fromIdTryMatch(Block a, Block b, IntOpenHashSet longs) {
        if (longs != null) {
            boolean contains = longs.contains(Registries.BLOCK.getRawId(b));
            if (contains) {
                return true;
            }
        } else  {
            IntOpenHashSet set = this.blockRawIds.get(b);
            if (set != null) {
                boolean contains = set.contains(Registries.BLOCK.getRawId(a));
                if (contains) {
                    return true;
                }
            }
        }
        return false;
    }
}
