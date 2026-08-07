package com.wishtoday.ts.simpleminer.core.blockBreaker.breakerFeatures;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreakerFeature;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Service
public class ToolBreakPreventer implements BlockBreakerFeature {
    private int damagePerBlock;
    private boolean isTool;

    public ToolBreakPreventer() {
        this.reset();
    }

    private void reset() {
        this.isTool = false;
        this.damagePerBlock = 0;
    }

    @Override
    public void beforeCycle(BlockBreakContext blockBreakContext) {
        this.reset();
        if (!blockBreakContext.getInfo().getCurrentIndividualConfig().isToolPreventBroken()) {
            return;
        }
        ToolComponent toolComponent = blockBreakContext.getMainHandStack().get(DataComponentTypes.TOOL);
        if (toolComponent != null) {
            this.damagePerBlock = toolComponent.damagePerBlock();
            this.isTool = true;
        }
    }

    @Override
    public boolean allowBreak(BlockBreakContext blockBreakContext) {
        if (!blockBreakContext.getInfo().getCurrentIndividualConfig().isToolPreventBroken()) {
            return true;
        }
        ItemStack stack = blockBreakContext.getMainHandStack();
        if (stack.isEmpty() || !isTool) {
            return true;
        }
        int i = stack.getMaxDamage() - stack.getDamage();
        return i > this.damagePerBlock;
    }

    @Override
    public boolean allowCollectItem(BlockBreakContext blockBreakContext, CollectContext collectContext) {
        if (!blockBreakContext.getInfo().getCurrentIndividualConfig().isToolPreventBroken()) {
            return true;
        }
        ItemStack stack = blockBreakContext.getMainHandStack();
        if (stack.isEmpty() || !isTool) {
            return true;
        }
        int i = stack.getMaxDamage() - stack.getDamage();
        return i > this.damagePerBlock;
    }

    @Override
    public boolean afterBlockBreakAllowContinue(BlockBreakContext blockBreakContext) {
        if (!blockBreakContext.getInfo().getCurrentIndividualConfig().isToolPreventBroken()) {
            return true;
        }
        ItemStack stack = blockBreakContext.getMainHandStack();
        if (stack.isEmpty() || !isTool) {
            return true;
        }
        int i = stack.getMaxDamage() - stack.getDamage();
        return i > this.damagePerBlock;
    }
}
