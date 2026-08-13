package com.wishtoday.ts.simpleminer.core.matcher;


import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.*;

@Service
public class BlockMatcher implements Reloadable {
    private final List<BlockFamily> families;
    private final FamilyCreator creator;

    public BlockMatcher(FamilyCreator creator, ServerConfig serverConfig) {
        this.families = new ArrayList<>();
        this.creator = creator;
        this.reload(serverConfig);
    }

    public void addFamily(BlockFamily family) {
        this.families.add(family);
    }

    public boolean match(Block a, Block b) {
        if (a == b) return true;
        for (BlockFamily family : this.families) {
            if (family.match(a, b)) {
                return true;
            }
        }
        return false;
    }

    public boolean match(BlockState a, BlockState b) {
        return this.match(a.getBlock(), b.getBlock());
    }

    @Override
    public boolean reload(ServerConfig config) {
        this.families.clear();
        List<String> blockFamilies = config.getBlockFamilies();
        for (String blockFamily : blockFamilies) {
            BlockFamily fromString = this.creator.createFromString(blockFamily);
            if (fromString != null) {
                this.families.add(fromString);
            }
        }
        return true;
    }
}
