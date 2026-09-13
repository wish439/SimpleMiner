package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.simpleservices.services.Container;
import com.wishtoday.simpleservices.services.annotation.ServiceField;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.DroppedCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ItemCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.MixinDependCollector;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerEntityManager.class)
public class INTERCEPT_ServerEntityManagerMixin {
    @ServiceField
    private ItemCollectorRouter router;

    @Unique
    private static final CollectContext CONTEXT = new CollectContext();

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;toLong(Lnet/minecraft/util/math/BlockPos;)J"), cancellable = true)
    private <T extends EntityLike> void addEntity(T entity
            , boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (router == null) {
            this.router = Container.getInstance().getFirst(ItemCollectorRouter.class).orElseThrow();
        }
        if (entity instanceof ItemEntity item) {
            DroppedCollector collector = router.getCollector();
            if (!(collector instanceof MixinDependCollector mixinDependCollector)) return;
            CONTEXT.setWorld(item.getWorld());
            CONTEXT.setEntity(item);
            if (mixinDependCollector.shouldCollectItemFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin")) {
                mixinDependCollector.collectItemFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin");
                cir.setReturnValue(true);
            }
            return;
        }
        if (entity instanceof ExperienceOrbEntity experienceOrbEntity) {
            DroppedCollector collector = router.getCollector();
            if (!(collector instanceof MixinDependCollector mixinDependCollector)) return;
            CONTEXT.setWorld(experienceOrbEntity.getWorld());
            CONTEXT.setEntity(experienceOrbEntity);
            if (mixinDependCollector.shouldCollectItemFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin")) {
                mixinDependCollector.collectExperienceFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin");
                cir.setReturnValue(true);
            }
        }
    }
}
