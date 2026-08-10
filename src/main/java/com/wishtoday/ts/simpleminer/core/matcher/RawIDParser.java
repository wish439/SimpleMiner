package com.wishtoday.ts.simpleminer.core.matcher;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RawIDParser implements MatcherParser<Integer>{
    @Override
    public boolean shouldParse(@NotNull String value) {
        Identifier identifier = Identifier.tryParse(value);
        return identifier != null;
    }

    @Override
    public Integer parse(String string) {
        Identifier identifier = Identifier.tryParse(string);
        Block block = Registries.BLOCK.get(identifier);
        return Registries.BLOCK.getRawId(block);
    }
}
