package com.wishtoday.ts.simpleminer.core.matcher;

import com.wishtoday.ts.simpleminer.mixin.Accessor.RegistryEntryReferenceAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BlockFamily {
    private final IntOpenHashSet allowedIds;
    private final IntOpenHashSet deniedIds;
    private final Set<TagKey<Block>> allowedTags;
    private final Set<TagKey<Block>> deniedTags;
    private final Map<Block, Set<TagKey<Block>>> tagCache;

    public BlockFamily(IntOpenHashSet allowedIds, IntOpenHashSet deniedIds, Set<TagKey<Block>> allowedTags, Set<TagKey<Block>> deniedTags) {
        this.allowedIds = allowedIds;
        this.deniedIds = deniedIds;
        this.allowedTags = allowedTags;
        this.deniedTags = deniedTags;
        this.tagCache = new ConcurrentHashMap<>();
    }

    private Set<TagKey<Block>> getOrCreateTags(Block a) {
        Set<TagKey<Block>> aTags;
        if (tagCache.containsKey(a)) {
            aTags = tagCache.get(a);
        } else {
            aTags = this.getFilteredTags(a);
            tagCache.put(a, aTags);
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

    public boolean match(Block a, Block b) {
        if (a == b) {
            return true;
        }
        return this.isAllowed(a) && this.isAllowed(b);
    }

    public boolean isAllowed(Block a) {
        if (this.deniedIds.contains(Registries.BLOCK.getRawId(a))) {
            return false;
        }
        if (this.allowedIds.contains(Registries.BLOCK.getRawId(a))) {
            return true;
        }
        Set<TagKey<Block>> aTags = getOrCreateTags(a);
        for (TagKey<Block> tag : aTags) {
            if (this.deniedTags.contains(tag)) {
                return false;
            }
            if (this.allowedTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }
}
