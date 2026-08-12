package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Name;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.crop.SingleCropBlockHandler;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

@Name("NOBLOCKITEM")
@Service
public class VanillaRightClickWithoutBlockItemHandler implements RightClickHandler {
    private final SingleCropBlockHandler  singleCropBlockHandler;

    @CreateConstruction
    public VanillaRightClickWithoutBlockItemHandler(SingleCropBlockHandler singleCropBlockHandler) {
        this.singleCropBlockHandler = singleCropBlockHandler;
    }

    @Override
    public ActionResult onUse(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, boolean isOutside, ItemStackCollector collector) {
        ItemStack stack = player.getStackInHand(hand);
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getBlock().isEnabled(world.getEnabledFeatures())) {
            return ActionResult.FAIL;
        } else if (player.interactionManager.getGameMode() == GameMode.SPECTATOR) {
            NamedScreenHandlerFactory namedScreenHandlerFactory = blockState.createScreenHandlerFactory(world, blockPos);
            if (namedScreenHandlerFactory != null) {
                player.openHandledScreen(namedScreenHandlerFactory);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        } else {
            boolean bl = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean bl2 = player.shouldCancelInteraction() && bl;
            ItemStack itemStack = stack.copy();
            if (!bl2) {
                ItemActionResult itemActionResult = this.onUseWithItemWithoutBlockItem(blockState, player.getStackInHand(hand), world, player, hand, hitResult, collector);
                if (itemActionResult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                    return itemActionResult.toActionResult();
                }

                if (itemActionResult == ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == Hand.MAIN_HAND) {
                    ActionResult actionResult = blockState.onUse(world, player, hitResult);
                    if (actionResult.isAccepted()) {
                        Criteria.DEFAULT_BLOCK_USE.trigger(player, blockPos);
                        return actionResult;
                    }
                }
            }

            if (!stack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                ItemUsageContext itemUsageContext = new ItemUsageContext(player, hand, hitResult);
                ActionResult actionResult;
                if (this.isCreative(player)) {
                    int i = stack.getCount();
                    actionResult = this.useOnBlockWithoutBlockItem(stack, itemUsageContext);
                    stack.setCount(i);
                } else {
                    actionResult = this.useOnBlockWithoutBlockItem(stack, itemUsageContext);
                }

                if (actionResult.isAccepted()) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(player, blockPos, itemStack);
                }

                return actionResult;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    private ItemActionResult onUseWithItemWithoutBlockItem(BlockState blockState, ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, ItemStackCollector collector) {
        /*if (stack.getItem() instanceof BlockItem) {
            return ItemActionResult.FAIL;
        }*/
        this.singleCropBlockHandler.tryHandle(player, hitResult.getBlockPos(), blockState, collector);
        return blockState.onUseWithItem(player.getStackInHand(hand), world, player, hand, hitResult);
    }

    private ActionResult useOnBlockWithoutBlockItem(ItemStack stack, ItemUsageContext itemUsageContext) {
        if (stack.getItem() instanceof BlockItem) {
            return ActionResult.PASS;
        }
        return stack.useOnBlock(itemUsageContext);
    }

    private boolean isCreative(ServerPlayerEntity player) {
        return player.interactionManager.getGameMode().isCreative();
    }
}
