package com.wishtoday.ts.simpleminer.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.wishtoday.simpleservices.services.Container;
import com.wishtoday.simpleservices.services.ServiceFieldType;
import com.wishtoday.simpleservices.services.ServiceInjector;
import com.wishtoday.simpleservices.services.annotation.ServiceClass;
import com.wishtoday.simpleservices.services.annotation.ServiceField;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector.ItemCollectorRouter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public class PUREAPI_BlockMixin {
    @ServiceField
    private ItemCollectorRouter router;
    @Unique
    private static final CollectContext CONTEXT = new CollectContext();
    @WrapWithCondition(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"))
    public boolean afterBreak(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool) {
        //No way......Because Block is before onInitialize.
        if (router == null) {
            router = Container.getInstance().getFirst(ItemCollectorRouter.class)
                    .orElseThrow();
        }
        CONTEXT.setPos(pos);
        CONTEXT.setWorld(world);
        //return true;
        return !router.shouldApplyMixin(CONTEXT, "PUREAPI_BlockMixin");
    }

    @WrapWithCondition(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"))
    public boolean afterBreak2(PlayerEntity instance, float exhaustion) {
        return !BlockBreaker.getBlockBreaking();
    }
}
