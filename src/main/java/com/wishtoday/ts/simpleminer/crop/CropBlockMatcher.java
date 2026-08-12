package com.wishtoday.ts.simpleminer.crop;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.core.matcher.BlockFamily;
import com.wishtoday.ts.simpleminer.core.matcher.FamilyCreator;
import net.minecraft.block.Block;

import java.util.List;

@Service
public class CropBlockMatcher implements Reloadable {
    private BlockFamily family;
    private final FamilyCreator creator;

    public boolean isSupportedCrop(Block block) {
        return this.family.isAllowed(block);
    }

    @CreateConstruction
    public CropBlockMatcher(FamilyCreator creator, ServerConfig config) {
        this.creator = creator;
        this.reload(config);
    }

    @Override
    public boolean reload(ServerConfig config) {
        List<String> a = config.getSupportCrops();
        this.family = this.creator.createFromList(a);
        return true;
    }
}
