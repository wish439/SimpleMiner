package com.wishtoday.ts.simpleminer.undo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UndoHistory {
    private final Map<UUID, UndoStorage> undoStorages;

    public UndoHistory() {
        this.undoStorages = new HashMap<>();
    }

    public void removeUndoStorage(UUID uuid) {
        this.undoStorages.remove(uuid);
    }

    public Collection<UndoStorage> getUndoStorages() {
        return undoStorages.values();
    }

    public UndoStorage getUndoStorage(UUID uuid) {
        return this.undoStorages.get(uuid);
    }

    public void addUndoStorage(UndoStorage undoStorage) {
        this.undoStorages.put(undoStorage.getUuid(), undoStorage);
    }
}
