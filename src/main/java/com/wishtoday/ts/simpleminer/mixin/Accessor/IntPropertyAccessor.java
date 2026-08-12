package com.wishtoday.ts.simpleminer.mixin.Accessor;

import net.minecraft.state.property.IntProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntProperty.class)
public interface IntPropertyAccessor {
    @Accessor
    int getMax();
    @Accessor
    int getMin();
}
