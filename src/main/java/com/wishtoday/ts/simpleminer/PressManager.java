package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

@Service
public class PressManager {
    private final Map<UUID, PlayerMinerInfo> playerMinerInfos;
    @CreateConstruction
    public PressManager() {
        this.playerMinerInfos = new HashMap<>();
    }

    public void togglePlayerState(boolean state, PlayerEntity player, int index) {
        if (playerMinerInfos.containsKey(player.getUuid()) && !state) {
            this.playerMinerInfos.remove(player.getUuid());
            return;
        }
        this.playerMinerInfos.put(player.getUuid(), new PlayerMinerInfo(index, state, player));
    }

    private PlayerMinerInfo getPlayerMinerInfo(UUID uuid) {
        return this.playerMinerInfos.get(uuid);
    }

    public PlayerMinerInfo getPlayerMinerInfo(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid());
    }

    public Collection<PlayerMinerInfo> getPlayerMinerInfos() {
        return Collections.unmodifiableCollection(this.playerMinerInfos.values());
    }
}
