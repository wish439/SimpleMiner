package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.simpleservices.services.ServiceFieldType;
import com.wishtoday.simpleservices.services.annotation.ServiceClass;
import com.wishtoday.simpleservices.services.annotation.ServiceField;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollectorRouter;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ServiceClass(ServiceFieldType.STATIC)
@Mixin(ItemScatterer.class)
public class PUREAPI_ItemScatterMixin {
    @ServiceField
    private static ItemCollectorRouter router;
    @Unique
    private static final CollectContext CONTEXT = new CollectContext();
    @Inject(method = "onStateReplaced", at = @At("HEAD"), cancellable = true)
    private static void onStateReplaced(BlockState state, BlockState newState, World world, BlockPos pos, CallbackInfo ci) {
        if (BlockBreaker.getBlockBreaking()) {
            CONTEXT.setPos(pos);CONTEXT.setWorld(world);
            if (!router.shouldApplyMixin(CONTEXT, "PUREAPI_ItemScatterMixin")) return;
            ci.cancel();
        }
    }

    @Inject(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/inventory/Inventory;)V", at = @At("HEAD"), cancellable = true)
    private static void onSpawn(World world, double x, double y, double z, Inventory inventory, CallbackInfo ci) {
        if (BlockBreaker.getBlockBreaking()) {
            CONTEXT.setPos(new BlockPos((int) x, (int) y, (int) z));CONTEXT.setWorld(world);
            if (!router.shouldApplyMixin(CONTEXT, "PUREAPI_ItemScatterMixin")) return;
            ci.cancel();
        }
    }

    @Inject(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void onSpawn1(World world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (BlockBreaker.getBlockBreaking()) {
            CONTEXT.setPos(new BlockPos((int) x, (int) y, (int) z));CONTEXT.setWorld(world);
            if (!router.shouldApplyMixin(CONTEXT, "PUREAPI_ItemScatterMixin")) return;
            ci.cancel();
        }
    }

    @Inject(method = "spawn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V", at = @At("HEAD"), cancellable = true)
    private static void onSpawn2(World world, BlockPos pos, DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (BlockBreaker.getBlockBreaking()) {
            CONTEXT.setPos(pos);CONTEXT.setWorld(world);
            if (!router.shouldApplyMixin(CONTEXT, "PUREAPI_ItemScatterMixin")) return;
            ci.cancel();
        }
    }
}