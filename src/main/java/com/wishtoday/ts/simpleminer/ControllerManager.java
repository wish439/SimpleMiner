package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.data.GuiSession;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.data.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.undo.UndoHistory;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

@Service
public class ControllerManager {
    private final Map<UUID, PlayerMinerInfo> playerMinerInfos;
    private final Set<UUID> pressedPlayer;
    @CreateConstruction
    public ControllerManager() {
        this.playerMinerInfos = new HashMap<>();
        this.pressedPlayer = new HashSet<>();
    }

    public void removePlayerMinerInfo(UUID uuid) {
        this.playerMinerInfos.remove(uuid);
    }

    public PlayerMinerInfo togglePlayerState(boolean state, PlayerEntity player, int index) {
        if (pressedPlayer.contains(player.getUuid()) && !state) {
            this.pressedPlayer.remove(player.getUuid());
        }
        if (state) {
            this.pressedPlayer.add(player.getUuid());
        }
        if (playerMinerInfos.containsKey(player.getUuid())) {
            PlayerMinerInfo info = this.getPlayerMinerInfo(player);
            MinerShapeInfo shapeInfo = info.getCurrentMinerShapeInfo();
            shapeInfo.setKeyPressed(state);
            shapeInfo.setCurrentShape(index);
            return info;
        }
        PlayerMinerInfo info = new PlayerMinerInfo(index, state, player, new IndividualConfig());
        this.playerMinerInfos.put(player.getUuid(), info);
        return info;
    }

    private PlayerMinerInfo getPlayerMinerInfo(UUID uuid) {
        return this.playerMinerInfos.get(uuid);
    }

    public PlayerMinerInfo getPlayerMinerInfo(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid());
    }

    public MinerShapeInfo getMinerShapeInfo(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid()).getCurrentMinerShapeInfo();
    }

    public MinerShapeInfo getMinerShapeInfo(UUID uuid) {
        return this.playerMinerInfos.get(uuid).getCurrentMinerShapeInfo();
    }

    public IndividualConfig getIndividualConfig(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid()).getCurrentIndividualConfig();
    }

    public IndividualConfig getIndividualConfig(MinerShapeInfo minerShapeInfo) {
        return this.getIndividualConfig(minerShapeInfo.getPlayer());
    }

    public UndoHistory getUndoHistory(PlayerEntity player) {
        return this.playerMinerInfos.get(player.getUuid()).getUndoHistory();
    }

    public MinerShapeInfo getPressedMinerShapeInfo(PlayerEntity player) {
        MinerShapeInfo info = this.getMinerShapeInfo(player);
        if (info == null) return null;
        if (!info.isKeyPressed()) return null;
        return info;
    }

    public GuiSession getSession(PlayerEntity player) {
        return this.getPlayerMinerInfo(player).getSession();
    }

    public Collection<PlayerMinerInfo> getPlayerMinerInfos() {
        return Collections.unmodifiableCollection(this.playerMinerInfos.values());
    }

    public Collection<MinerShapeInfo> filterPressesMinerShapeInfo() {
        return this.pressedPlayer.stream().map(this::getMinerShapeInfo).filter(Objects::nonNull).toList();
    }
}
