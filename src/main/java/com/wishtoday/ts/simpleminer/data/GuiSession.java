package com.wishtoday.ts.simpleminer.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GuiSession {
    @Nullable
    private UUID uid;

    public UUID generateUUID() {
        this.uid = UUID.randomUUID();
        return this.uid;
    }

    @SuppressWarnings("ConstantValue")
    public boolean consumeUUID(@NotNull UUID uuid) {
        if (uuid == null) return false;
        if (!uuid.equals(this.uid)) {
            return false;
        }
        this.uid = null;
        return true;
    }
}
