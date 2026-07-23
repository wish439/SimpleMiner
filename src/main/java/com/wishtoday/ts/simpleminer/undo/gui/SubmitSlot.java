package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import lombok.Getter;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SubmitSlot extends Slot {
    @Getter
    private final UndoGuiStorageContext undoStorage;
    public SubmitSlot(Inventory inventory, int index, int x, int y, UndoGuiStorageContext undoStorage) {
        super(inventory, index, x, y);
        this.undoStorage = undoStorage;
    }

    @Override
    public void setStack(ItemStack stack, ItemStack previousStack) {
        ItemStackKey key = new ItemStackKey(stack);

        undoStorage.addCurrentCountTo(key, info -> Math.min(stack.getCount(), info.getMaxCount() - info.getCurrentCount()));
        /*MaterialInfo info = undoStorage.getInfo(itemStackKey);
        if (info == null) {
            System.out.println("setStack null check triggered");
            return;
        }
        int i = stack.getCount();
        int remain = info.getMaxCount() - info.getCurrentCount();
        int min = Math.min(i, remain);
        info.addCurrentCount(min);*/
        //stack.decrement(min);
        //super.setStack(stack, previousStack);
    }

    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasStack() {
        return false;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        //return true;
        ItemStackKey key = new ItemStackKey(stack);
        MaterialInfo info = this.undoStorage.getInfo(key);
        if (info == null) return false;
        return !info.isFinished();
        //return info.getCurrentCount() + stack.getCount() <= info.getMaxCount();
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        ItemStackKey key = new ItemStackKey(stack);
        int i = undoStorage.addCurrentCountTo(key, stack.getCount());
        if (i >= 0) {
            stack.setCount(i);
        }
        if (i >= 0) {
            return stack;
        }
        //stack.decrement(min);
        return ItemStack.EMPTY;
        //return ItemStack.EMPTY;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        ItemStackKey itemStackKey = new ItemStackKey(stack);
        MaterialInfo info = this.undoStorage.getInfo(itemStackKey);
        if (info == null) {
            return 0;
        }
        int i = info.getMaxCount() - info.getCurrentCount();
        return Math.min(i, super.getMaxItemCount(stack));
    }
}
