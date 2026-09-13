package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.core.blockBreaker.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;

@Name("INTERCEPT")
@Service
public class InterceptItemCollector implements DroppedCollector, MixinDependCollector {
    private final Object2IntOpenHashMap<ItemStackKey> map;
    private final ExperienceCollector experienceCollector;

    @CreateConstruction
    public InterceptItemCollector(ExperienceCollector experienceCollector) {
        this.experienceCollector = experienceCollector;
        this.map = new Object2IntOpenHashMap<>();
    }

    @Override
    public void start() {
        this.map.clear();
        this.experienceCollector.initialize();
    }

    @Override
    public boolean shouldCollect(CollectContext context) {
        return false;
    }

    @Override
    public void collectItem(CollectContext context) {

    }

    @Override
    public boolean shouldCollectItemFromMixin(CollectContext context, String mixinName) {
        return context.getPos() == null
                && context.getWorld() != null
                && context.getEntity() != null
                && context.getHandStack() == null
                && context.getPlayer() == null
                && mixinName.startsWith("INTERCEPT")
                && BlockBreaker.getBlockBreaking();
    }

    @Override
    public boolean shouldCollectExperienceFromMixin(CollectContext context, String mixinName) {
        return context.getPos() == null
                && context.getWorld() != null
                && context.getEntity() != null
                && context.getHandStack() == null
                && context.getPlayer() == null
                && mixinName.startsWith("INTERCEPT")
                && BlockBreaker.getBlockBreaking();
    }

    @Override
    public void collectExperienceFromMixin(CollectContext context, String mixinName) {
        Entity entity = context.getEntity();
        if (!(entity instanceof ExperienceOrbEntity e)) return;
        int experienceAmount = e.getExperienceAmount();
        if (experienceAmount <= 0) return;
        this.experienceCollector.consumeExperience(experienceAmount);
    }

    @Override
    public void collectItemFromMixin(CollectContext context, String mixinName) {
        Entity itemEntity = context.getEntity();
        if (!(itemEntity instanceof ItemEntity item)) return;
        this.map.addTo(new ItemStackKey(item.getStack()), item.getStack().getCount());
    }

    @Override
    public CollectedResult finish() {
        return new CollectedResult(new Object2IntOpenHashMap<>(map), this.experienceCollector.getExperience());
    }

    @Override
    public boolean shouldApplyMixin(CollectContext context
            , String mixinName) {
        return context.getPos() == null
                && context.getWorld() != null
                && context.getEntity() != null
                && context.getHandStack() == null
                && context.getPlayer() == null
                && mixinName.startsWith("INTERCEPT");
    }

    @Override
    public void collectExperience(CollectContext context) {

    }
}
