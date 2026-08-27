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
import java.util.function.Consumer;

@Service
public class FamilyCreator {
    public BlockFamily createFromString(String s) {
        return this.createFromList(Arrays.stream(s.split(",")).toList());
    }

    public BlockFamily createFromList(List<String> list) {
        Set<TagKey<Block>> tags = new HashSet<>();
        Set<TagKey<Block>> deniedTags = new HashSet<>();
        IntOpenHashSet allowedIds = new IntOpenHashSet();
        IntOpenHashSet deniedIds = new IntOpenHashSet();
        for (String s : list) {
            s = s.toLowerCase();
            boolean isOpposite = false;
            if (s.startsWith("!")) {
                s = s.substring(1);
                isOpposite = true;
            }
            if (s.startsWith("#")) {
                TagKey<Block> tagKey = this.parseTag(s);
                if (tagKey == null) {
                    continue;
                }
                if (isOpposite) deniedTags.add(tagKey);
                else tags.add(tagKey);
                continue;
            }
            int i = this.parseBlock(s);
            if (i == -1) {
                continue;
            }
            if (isOpposite) deniedIds.add(i);
            else allowedIds.add(i);
            /*if (s.startsWith("#")) {
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
            continue;*/
        }
        return new BlockFamily(allowedIds, deniedIds, tags, deniedTags);
    }

    private TagKey<Block> parseTag(String entry) {
        entry = entry.substring(1);
        Identifier id = Identifier.tryParse(entry);
        if (id == null) {
            return null;
        }
        return this.parseBlockTag(id);
    }

    private int parseBlock(String entry) {
        Identifier id = Identifier.tryParse(entry);
        if (id == null) {
            return -1;
        }
        return this.parseBlockRawID(id);
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
