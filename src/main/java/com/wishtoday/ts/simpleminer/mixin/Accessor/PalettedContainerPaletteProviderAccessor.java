package com.wishtoday.ts.simpleminer.mixin.Accessor;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.PaletteProvider.class)
public interface PalettedContainerPaletteProviderAccessor {
    @Accessor
    int getEdgeBits();
}
