package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

@Service
public class PressManager {
    private final Map<UUID, PlayerMinerInfo> playerMinerInfos;
    private final Set<UUID> pressedPlayer;
    @CreateConstruction
    public PressManager() {
        this.playerMinerInfos = new HashMap<>();
        this.pressedPlayer = new HashSet<>();
    }

    public void removePlayerMinerInfo(UUID uuid) {
        this.playerMinerInfos.remove(uuid);
    }

    public void togglePlayerState(boolean state, PlayerEntity player, int index) {
        if (pressedPlayer.contains(player.getUuid()) && !state) {
            this.pressedPlayer.remove(player.getUuid());
        }
        if (state) {
            this.pressedPlayer.add(player.getUuid());
        }
        if (playerMinerInfos.containsKey(player.getUuid())) {
            PlayerMinerInfo info = this.playerMinerInfos.get(player.getUuid());
            info.setKeyPressed(state);
            info.setCurrentShape(index);
            return;
        }
        this.playerMinerInfos.put(player.getUuid(), new PlayerMinerInfo(index, state, player, new IndividualConfig()));
    }

    private PlayerMinerInfo getPlayerMinerInfo(UUID uuid) {
        return this.playerMinerInfos.get(uuid);
    }

    public PlayerMinerInfo getPlayerMinerInfo(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid());
    }

    public PlayerMinerInfo getPressedPlayerMinerInfo(PlayerEntity player) {
        PlayerMinerInfo info = this.playerMinerInfos.get(player.getUuid());
        if (info == null) return null;
        if (!info.isKeyPressed()) return null;
        return info;
    }

    public Collection<PlayerMinerInfo> getPlayerMinerInfos() {
        return Collections.unmodifiableCollection(this.playerMinerInfos.values());
    }

    public Collection<PlayerMinerInfo> filterPressesPlayerInfos() {
        return this.pressedPlayer.stream().map(this.playerMinerInfos::get).filter(Objects::nonNull).toList();
    }
}
