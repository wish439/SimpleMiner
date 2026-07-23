package com.wishtoday.ts.simpleminer.undo.gui;

import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.MaterialInfo;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

@Data
@AllArgsConstructor
public class UndoGuiStorageContext {
    private Map<ItemStackKey, MaterialInfo> undoStorage;
    private int completedCount;

    public UndoGuiStorageContext() {
        this.undoStorage = new HashMap<>();
    }

    public MaterialInfo getInfo(ItemStackKey key) {
        return undoStorage.get(key);
    }

    public int addCurrentCountTo(ItemStackKey key, int value) {
        if (value < 0) {
            return this.removeCurrentCountFrom(key, i -> value);
        }
        return this.addCurrentCountTo(key, i -> value);
    }

    public boolean isFully() {
        return this.completedCount >= this.undoStorage.size();
    }

    public void refreshCompletedCount() {
        this.completedCount = 0;
        this.undoStorage.forEach((s, materialInfo) -> {
            if (materialInfo.isFinished()) this.completedCount++;
        });
    }

    public int getRemain(ItemStackKey key) {
        MaterialInfo info = this.getInfo(key);
        return info.getMaxCount() - info.getCurrentCount();
    }

    public int addCurrentCountTo(ItemStackKey key, ToIntFunction<MaterialInfo> valueFactory) {
        MaterialInfo info = this.getInfo(key);
        if (info == null) return -1;
        int i = valueFactory.applyAsInt(info);
        if (info.isFinished()) return i;
        int j = Math.min(this.getRemain(key), i);
        info.addCurrentCount(j);
        if (info.isFinished()) completedCount++;
        return i - j;
    }

    public int removeCurrentCountFrom(ItemStackKey key, ToIntFunction<MaterialInfo> valueFactory) {
        MaterialInfo info = this.getInfo(key);
        if (info == null) return -1;
        int i = valueFactory.applyAsInt(info);
        boolean finished = info.isFinished();
        int currentCount = info.getCurrentCount();
        int j = Math.max(i, -currentCount);
        info.addCurrentCount(j);
        if (!info.isFinished() && finished) completedCount--;
        return i - j;
    }

    public void saveToStorage(PressManager pressManager, PlayerEntity player) {
        if (pressManager == null) return;
        PlayerMinerInfo info = pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        UndoStorage storage = info.getUndoStorage();
        if (storage == null) return;
        storage.setItems(this.undoStorage);
        storage.setCompletedCount(this.completedCount);
    }

    public void clearStorage() {
        this.undoStorage.clear();
    }

    public void resetStorage() {
        this.undoStorage.replaceAll((k, v) -> {
            v.setCurrentCount(0);
            return v;
        });
        this.completedCount = 0;
    }
}
