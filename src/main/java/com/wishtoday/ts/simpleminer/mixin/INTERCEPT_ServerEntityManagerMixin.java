package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.simpleservices.services.annotation.ServiceClass;
import com.wishtoday.simpleservices.services.annotation.ServiceField;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollectorRouter;
import com.wishtoday.ts.simpleminer.core.blockBreaker.MixinDependCollector;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ServiceClass
@Mixin(ServerEntityManager.class)
public class INTERCEPT_ServerEntityManagerMixin {
    @ServiceField
    private ItemCollectorRouter router;

    @Unique
    private static final CollectContext CONTEXT = new CollectContext();

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;toLong(Lnet/minecraft/util/math/BlockPos;)J"), cancellable = true)
    private <T extends EntityLike> void addEntity(T entity
            , boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof ItemEntity item)) return;
        ItemCollector collector = router.getCollector();
        if (!(collector instanceof MixinDependCollector mixinDependCollector)) return;
        CONTEXT.setWorld(item.getWorld());
        CONTEXT.setItemEntity(item);
        if (mixinDependCollector.shouldCollectItemFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin")) {
            mixinDependCollector.collectItemFromMixin(CONTEXT, "INTERCEPT_ServerEntityManagerMixin");
            cir.setReturnValue(true);
        }
    }
}
