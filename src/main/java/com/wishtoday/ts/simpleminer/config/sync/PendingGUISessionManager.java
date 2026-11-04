package com.wishtoday.ts.simpleminer.config.sync;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PendingGUISessionManager {
    private final Map<UUID, UUID> guids;

    @CreateConstruction
    public PendingGUISessionManager() {
        this.guids = new HashMap<>();
    }

    public boolean containsGUID(UUID uuid) {
        return this.guids.containsKey(uuid);
    }

    public boolean recordGUI(UUID uuid, PlayerEntity player) {
        return this.guids.put(uuid, player.getUuid()) != null;
    }

    public UUID generateUUIDToPlayer(PlayerEntity player) {
        UUID uuid = UUID.randomUUID();
        this.guids.put(uuid, player.getUuid());
        return uuid;
    }

    public UUID removeGUID(UUID uuid) {
        return this.guids.remove(uuid);
    }

    public void initialize() {
        this.guids.clear();
    }
}
