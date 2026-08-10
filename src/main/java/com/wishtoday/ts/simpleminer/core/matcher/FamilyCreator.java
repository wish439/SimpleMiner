package com.wishtoday.ts.simpleminer.core.matcher;

import com.wishtoday.simpleservices.services.annotation.Service;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FamilyCreator {
    public BlockFamily createFromString(String s) {
        return this.createFromList(Arrays.stream(s.split(",")).toList());
    }

    public BlockFamily createFromList(List<String> list) {
        Set<TagKey<Block>> tags = new HashSet<>();
        IntOpenHashSet allowedIds = new IntOpenHashSet();
        for (String s : list) {
            s = s.toLowerCase();
            if (s.startsWith("#")) {
                s = s.substring(1);
                Identifier id = Identifier.tryParse(s);
                if (id == null) {
                    continue;
                }
                TagKey<Block> e = this.parseBlockTag(id);
                if (e == null) {
                    continue;
                }
                tags.add(e);
                continue;
            }
            Identifier id = Identifier.tryParse(s);
            if (id == null) {
                continue;
            }
            int k = this.parseBlockRawID(id);
            if (k == -1) {
                continue;
            }
            allowedIds.add(k);
            continue;
        }
        return new BlockFamily(allowedIds, tags);
    }

    private int parseBlockRawID(Identifier id) {
        if (!Registries.BLOCK.containsId(id)) {
            return -1;
        }
        if (id.getPath().equals("air")) {
            return -1;
        }
        Block block = Registries.BLOCK.get(id);
        return Registries.BLOCK.getRawId(block);
    }
    private TagKey<Block> parseBlockTag(Identifier id) {
        return TagKey.of(RegistryKeys.BLOCK, id);
    }
}
