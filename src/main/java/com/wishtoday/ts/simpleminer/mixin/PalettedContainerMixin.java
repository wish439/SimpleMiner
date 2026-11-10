package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.ts.simpleminer.mixinInterface.PalettedContainerTestExtension;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> implements PalettedContainerTestExtension<T> {

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Shadow
    public abstract void lock();

    @Shadow
    @Final
    private PalettedContainer.PaletteProvider paletteProvider;

    @Shadow
    public abstract void unlock();

    @Override
    public T simpleminer$swap(int x, int y, int z, T value) {
        this.lock();

        Object var5;
        try {
            var5 = this.simpleminer$swap(this.paletteProvider.computeIndex(x, y, z), value);
        } finally {
            this.unlock();
        }

        return (T)var5;
    }

    @Override
    public T simpleminer$swap(int index, T value) {
        int i = this.data.palette().index(value);
        this.data.storage().set(index, i);
        return null;
    }
}
