package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

@Name("INTERCEPT")
@Service
public class InterceptItemCollector implements ItemCollector, MixinDependCollector {
    private final Object2IntOpenHashMap<ItemStackKey> map;

    @CreateConstruction
    public InterceptItemCollector() {
        this.map = new Object2IntOpenHashMap<>();
    }

    @Override
    public void start() {
        this.map.clear();
    }

    @Override
    public boolean shouldCollectItem(CollectContext context) {
        return false;
    }

    @Override
    public void collectItem(CollectContext context) {

    }

    @Override
    public boolean shouldCollectItemFromMixin(CollectContext context, String mixinName) {
        return context.getPos() == null
                && context.getWorld() != null
                && context.getItemEntity() != null
                && context.getHandStack() == null
                && context.getPlayer() == null
                && mixinName.startsWith("INTERCEPT")
                && BlockBreaker.getBlockBreaking();
    }

    @Override
    public void collectItemFromMixin(CollectContext context, String mixinName) {
        Entity itemEntity = context.getItemEntity();
        if (!(itemEntity instanceof ItemEntity item)) return;
        this.map.addTo(new ItemStackKey(item.getStack()), item.getStack().getCount());
    }

    @Override
    public CollectedResult finish() {
        System.out.println("map: " + this.map);
        return new CollectedResult(new Object2IntOpenHashMap<>(map));
    }

    @Override
    public boolean shouldApplyMixin(CollectContext context
            , String mixinName) {
        return context.getPos() == null
                && context.getWorld() != null
                && context.getItemEntity() != null
                && context.getHandStack() == null
                && context.getPlayer() == null
                && mixinName.startsWith("INTERCEPT");
    }
}
