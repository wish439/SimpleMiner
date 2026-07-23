package com.wishtoday.ts.simpleminer.undo.gui.screenHandler;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.gui.MinerScreenHandlerTypes;
import com.wishtoday.ts.simpleminer.gui.EmptyInventory;
import com.wishtoday.ts.simpleminer.undo.gui.SubmitSlot;
import com.wishtoday.ts.simpleminer.undo.gui.UndoGuiStorageContext;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.Map;

public class UndoScreenHandler extends ScreenHandler {
    public UndoScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(MinerScreenHandlerTypes.UNDO, syncId);
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 146));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 88 + i * 18));
            }
        }

        this.undoStorage = new UndoGuiStorageContext();
        EmptyInventory inventory = new EmptyInventory();
        SubmitSlot slot = new SubmitSlot(inventory, 36, 80, 36, undoStorage);
        this.addSlot(slot);
        this.submitSlot = slot;
        this.pressManager = null;
    }

    public UndoScreenHandler(int syncId, PlayerInventory playerInventory, Map<ItemStackKey, MaterialInfo> undoMaterialInfoMap, PressManager pressManager, int completedCount) {
        this(syncId, playerInventory);
        this.setUndoStorage(undoMaterialInfoMap);
        this.pressManager = pressManager;
        this.setCompletedCount(completedCount);
    }

    @Getter
    private UndoGuiStorageContext undoStorage;
    private final SubmitSlot submitSlot;
    private PressManager pressManager;

    public void setUndoStorage(Map<ItemStackKey, MaterialInfo> undoStorage) {
        this.undoStorage.setUndoStorage(undoStorage);
        submitSlot.getUndoStorage().setUndoStorage(undoStorage);
    }

    public void setCompletedCount(int value) {
        this.undoStorage.setCompletedCount(value);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        Slot slot1 = this.getSlot(slot);
        ItemStack item = ItemStack.EMPTY;

        if (!slot1.hasStack()) {
            return item;
        }
        ItemStack original = slot1.getStack();
        item = original.copy();
        if (slot >= 9) {
            if (this.insertItem(original, 36, 37, true)) {
                //return copy;
                if (original.isEmpty()) {
                    slot1.setStack(ItemStack.EMPTY);
                } else {
                    slot1.markDirty();
                }
                return original;
            }
            if (!this.insertItem(original, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (this.insertItem(original, 36, 37, true)) {
                if (original.isEmpty()) {
                    slot1.setStack(ItemStack.EMPTY);
                } else {
                    slot1.markDirty();
                }
                return original;
            }
            if (!this.insertItem(original, 9, 36, false)) return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot1.setStack(ItemStack.EMPTY);
        } else {
            slot1.markDirty();
        }
        return item;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        if (player.getWorld().isClient) {
            return;
        }
        this.undoStorage.saveToStorage(pressManager, player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
