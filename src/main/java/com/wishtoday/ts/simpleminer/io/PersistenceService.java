package com.wishtoday.ts.simpleminer.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.config.SyncIndividualConfigS2CPayload;
import com.wishtoday.ts.simpleminer.undo.MaterialInfo;
import com.wishtoday.ts.simpleminer.undo.UndoDisplayInfo;
import com.wishtoday.ts.simpleminer.undo.UndoHistory;
import com.wishtoday.ts.simpleminer.undo.UndoStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 世界目录下的持久化服务:
 * <pre>
 *   &lt;save&gt;/simpleminer/
 *     serverconfig.json
 *     individualconfig/&lt;playerUuid&gt;.json
 *     undohistory/&lt;playerUuid&gt;/&lt;undoUuid&gt;.dat
 * </pre>
 * 文件 IO 全部走虚拟线程;主线程只做序列化快照与内存状态管理。
 * undo 策略:内存保留每个玩家最近 IN_MEMORY_UNDO 条完整记录,其余在磁盘按需加载,磁盘每玩家上限 MAX_UNDO_PER_PLAYER 条。
 */
@Service
public class PersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceService.class);

    private static final String MOD_DIR_NAME = "simpleminer";
    private static final int SAVE_INTERVAL_TICKS = 600; // 30 秒
    private static final int IN_MEMORY_UNDO_PER_PLAYER = 5;

    private final ExecutorService ioExecutor;
    private final Gson gson;

    private final ServerConfig serverConfig;
    private final PressManager pressManager;

    @Nullable
    private volatile MinecraftServer server;
    /**
     * 玩家 -> 磁盘上的 undo 记录 uuid(主线程维护)
     */
    private final Map<UUID, Set<UUID>> undoOnDisk;

    @CreateConstruction
    public PersistenceService(ServerConfig serverConfig, PressManager pressManager) {
        this.ioExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.serverConfig = serverConfig;
        this.pressManager = pressManager;
        this.server = null;
        this.undoOnDisk = new HashMap<>();
    }

    @PostConstruct
    public void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.loadServerConfig();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(ignored -> this.saveAllSync());
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % SAVE_INTERVAL_TICKS == 0) {
                this.saveAllAsync();
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, ignoredServer) -> {
            ServerPlayerEntity player = handler.player;
            this.loadIndividualConfig(player);
            this.syncIndividualConfig(player);
            this.scanUndoOnDisk(player.getUuid());
        });
    }

    // ==================== 路径 ====================

    private Path root() {
        MinecraftServer current = this.server;
        if (current == null) throw new IllegalStateException("Server is not started");
        return current.getSavePath(WorldSavePath.ROOT).resolve(MOD_DIR_NAME);
    }

    private Path serverConfigPath() {
        return root().resolve("serverconfig.json");
    }

    private Path individualConfigPath(UUID playerUuid) {
        return root().resolve("individualconfig").resolve(playerUuid + ".json");
    }

    private Path undoHistoryDir(UUID playerUuid) {
        return root().resolve("undohistory").resolve(playerUuid.toString());
    }

    private Path undoPath(UUID playerUuid, UUID undoUuid) {
        return undoHistoryDir(playerUuid).resolve(undoUuid + ".dat");
    }

    // ==================== ServerConfig ====================

    private void loadServerConfig() {
        Path path = serverConfigPath();
        if (!Files.exists(path)) return;
        try {
            ServerConfig loaded = gson.fromJson(Files.readString(path), ServerConfig.class);
            if (loaded != null) {
                this.serverConfig.setFromConfig(loaded);
                LOGGER.info("Loaded server config from {}", path);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load server config from {}", path, e);
        }
    }

    public void saveServerConfigAsync() {
        String json = gson.toJson(this.serverConfig, ServerConfig.class);
        Path path = serverConfigPath();
        this.ioExecutor.execute(() -> this.writeJson(path, json));
    }

    // ==================== IndividualConfig ====================

    private void loadIndividualConfig(ServerPlayerEntity player) {
        Path path = individualConfigPath(player.getUuid());
        if (!Files.exists(path)) return;
        try {
            IndividualConfig loaded = gson.fromJson(Files.readString(path), IndividualConfig.class);
            if (loaded == null) return;
            PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
            if (info == null) {
                this.pressManager.togglePlayerState(false, player, 0);
                info = this.pressManager.getPlayerMinerInfo(player);
            }
            if (info != null) {
                info.setCurrentIndividualConfig(loaded);
                LOGGER.info("Loaded individual config for {} from {}", player.getUuid(), path);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load individual config from {}", path, e);
        }
    }

    private void syncIndividualConfig(ServerPlayerEntity player) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        IndividualConfig config = info.getCurrentIndividualConfig();
        ServerPlayNetworking.send(player, new SyncIndividualConfigS2CPayload(config));
    }

    public void saveIndividualConfigAsync(ServerPlayerEntity player) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        saveIndividualConfigAsync(player.getUuid(), info.getCurrentIndividualConfig());
    }

    public void saveIndividualConfigAsync(UUID playerUuid, IndividualConfig config) {
        String json = gson.toJson(config, IndividualConfig.class);
        Path path = individualConfigPath(playerUuid);
        this.ioExecutor.execute(() -> this.writeJson(path, json));
    }

    /**
     * 玩家断开时调用:将个人配置与该玩家内存中全部 undo 记录落盘(主线程序列化,虚拟线程写文件)。
     * 必须在 PlayerMinerInfo 被移除之前调用。
     */
    public void savePlayerData(ServerPlayerEntity player) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        UUID playerUuid = player.getUuid();
        String configJson = gson.toJson(info.getCurrentIndividualConfig(), IndividualConfig.class);
        Path configPath = individualConfigPath(playerUuid);
        List<Map.Entry<Path, NbtCompound>> undoWrites = new ArrayList<>();
        for (UndoStorage storage : info.getUndoHistory().getUndoStorages()) {
            NbtCompound tag = UndoStorageCodec.encode(storage, this.registryManager());
            undoWrites.add(Map.entry(undoPath(playerUuid, storage.getUuid()), tag));
        }
        this.ioExecutor.execute(() -> {
            this.writeJson(configPath, configJson);
            for (Map.Entry<Path, NbtCompound> entry : undoWrites) {
                this.writeUndo(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * 单条 undo 记录立即落盘(GUI 关闭写回后调用)。
     */
    public void saveUndoStorageAsync(ServerPlayerEntity player, UUID undoUuid) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) return;
        UndoStorage storage = info.getUndoHistory().getUndoStorage(undoUuid);
        if (storage == null) return;
        NbtCompound tag = UndoStorageCodec.encode(storage, this.registryManager());
        Path path = undoPath(player.getUuid(), undoUuid);
        this.ioExecutor.execute(() -> this.writeUndo(path, tag));
    }

    // ==================== Undo: 保存/加载/删除/索引 ====================

    /**
     * 新 undo 记录落盘 + 内存/磁盘淘汰。主线程调用。
     */
    public void onUndoRecordAdded(ServerPlayerEntity player, UndoStorage storage) {
        UUID playerUuid = player.getUuid();
        Set<UUID> disk = this.undoOnDisk.computeIfAbsent(playerUuid, k -> new HashSet<>());
        disk.add(storage.getUuid());

        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        int maxUndoRecords = this.effectiveMaxUndoRecords(player);
        if (info != null) {
            this.evictInMemory(info.getUndoHistory(), maxUndoRecords);
        }

        NbtCompound tag = UndoStorageCodec.encode(storage, this.registryManager());
        Path path = undoPath(playerUuid, storage.getUuid());
        this.ioExecutor.execute(() -> {
            try {
                Files.createDirectories(path.getParent());
                NbtIo.writeCompressed(tag, path);
            } catch (IOException e) {
                LOGGER.error("Failed to write undo record {}", path, e);
            }
        });

        // 磁盘上限淘汰(异步读时间删最旧),上限取个人配置与服务器配置的较小值
        if (disk.size() > maxUndoRecords) {
            this.evictOnDiskAsync(playerUuid, maxUndoRecords);
        }
    }

    /**
     * 按需从磁盘加载完整 undo 记录,加载完成后回到主线程回调。
     */
    public void loadUndoAsync(UUID playerUuid, UUID undoUuid, BiConsumer<UndoStorage, Throwable> callback) {
        Path path = undoPath(playerUuid, undoUuid);
        MinecraftServer current = this.server;
        if (current == null) return;
        this.ioExecutor.execute(() -> {
            try {
                NbtCompound tag = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
                UndoStorage storage = UndoStorageCodec.decode(tag, this.registryManager());
                current.execute(() -> callback.accept(storage, null));
            } catch (Exception e) {
                LOGGER.error("Failed to load undo record {}", path, e);
                current.execute(() -> callback.accept(null, e));
            }
        });
    }

    public void removeUndoRecord(ServerPlayerEntity player, UUID undoUuid) {
        UUID playerUuid = player.getUuid();
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info != null) {
            info.getUndoHistory().removeUndoStorage(undoUuid);
        }
        Set<UUID> disk = this.undoOnDisk.get(playerUuid);
        if (disk != null) {
            disk.remove(undoUuid);
            if (disk.isEmpty()) this.undoOnDisk.remove(playerUuid);
        }
        Path path = undoPath(playerUuid, undoUuid);
        this.ioExecutor.execute(() -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                LOGGER.error("Failed to delete undo record {}", path, e);
            }
        });
    }

    /**
     * 收集该玩家全部 undo 列表(内存 + 磁盘),完成后主线程回调。
     */
    public void collectUndoList(ServerPlayerEntity player, Consumer<List<UndoDisplayInfo>> callback) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        if (info == null) {
            callback.accept(List.of());
            return;
        }
        UndoHistory history = info.getUndoHistory();
        List<UndoDisplayInfo> inMemory = history.getUndoStorages().stream()
                .map(storage -> new UndoDisplayInfo(undoPosText(storage), storage.getTime(), storage.getUuid(),
                        this.topStacks(storage), storage.getItems().size() > 3))
                .toList();

        UUID playerUuid = player.getUuid();
        Set<UUID> onDisk = new HashSet<>(this.undoOnDisk.getOrDefault(playerUuid, Set.of()));
        onDisk.removeIf(history::contains);
        if (onDisk.isEmpty()) {
            List<UndoDisplayInfo> sorted = new ArrayList<>(inMemory);
            sorted.sort(Comparator.comparingLong(UndoDisplayInfo::getTime).reversed());
            callback.accept(sorted);
            return;
        }

        MinecraftServer current = this.server;
        RegistryWrapper.WrapperLookup lookup = this.registryManager();
        this.ioExecutor.execute(() -> {
            List<UndoDisplayInfo> diskInfos = new ArrayList<>();
            for (UUID undoUuid : onDisk) {
                try {
                    NbtCompound tag = NbtIo.readCompressed(undoPath(playerUuid, undoUuid), NbtSizeTracker.ofUnlimitedBytes());
                    diskInfos.add(UndoStorageCodec.decodeMeta(tag, lookup));
                } catch (Exception e) {
                    LOGGER.error("Failed to load undo meta {}", undoPath(playerUuid, undoUuid), e);
                }
            }
            List<UndoDisplayInfo> all = new ArrayList<>(inMemory);
            all.addAll(diskInfos);
            all.sort(Comparator.comparingLong(UndoDisplayInfo::getTime).reversed());
            current.execute(() -> callback.accept(all));
        });
    }

    // ==================== 定时/停止保存 ====================

    public void saveAllAsync() {
        MinecraftServer current = this.server;
        if (current == null) return;
        List<Runnable> writeTasks = new ArrayList<>();
        writeTasks.add(() -> this.writeJson(serverConfigPath(), gson.toJson(this.serverConfig, ServerConfig.class)));
        for (PlayerMinerInfo info : this.pressManager.getPlayerMinerInfos()) {
            UUID playerUuid = info.getPlayer().getUuid();
            IndividualConfig config = info.getCurrentIndividualConfig();
            String json = gson.toJson(config, IndividualConfig.class);
            writeTasks.add(() -> this.writeJson(individualConfigPath(playerUuid), json));
            for (UndoStorage storage : info.getUndoHistory().getUndoStorages()) {
                NbtCompound tag = UndoStorageCodec.encode(storage, this.registryManager());
                Path path = undoPath(playerUuid, storage.getUuid());
                writeTasks.add(() -> this.writeUndo(path, tag));
            }
        }
        this.ioExecutor.execute(() -> writeTasks.forEach(Runnable::run));
    }

    private void saveAllSync() {
        if (this.server == null) return;
        try {
            Files.createDirectories(root());
            this.writeJson(serverConfigPath(), gson.toJson(this.serverConfig, ServerConfig.class));
            for (PlayerMinerInfo info : this.pressManager.getPlayerMinerInfos()) {
                UUID playerUuid = info.getPlayer().getUuid();
                this.writeJson(individualConfigPath(playerUuid), gson.toJson(info.getCurrentIndividualConfig(), IndividualConfig.class));
                for (UndoStorage storage : info.getUndoHistory().getUndoStorages()) {
                    this.writeUndo(undoPath(playerUuid, storage.getUuid()), UndoStorageCodec.encode(storage, this.registryManager()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save all data", e);
        }
    }

    // ==================== 内部工具 ====================

    private void evictInMemory(UndoHistory history, int maxUndoRecords) {
        int memLimit = Math.min(IN_MEMORY_UNDO_PER_PLAYER, maxUndoRecords);
        while (history.size() > memLimit) {
            UUID oldest = null;
            long oldestTime = Long.MAX_VALUE;
            for (UndoStorage storage : history.getUndoStorages()) {
                if (storage.getTime() < oldestTime) {
                    oldestTime = storage.getTime();
                    oldest = storage.getUuid();
                }
            }
            if (oldest == null) break;
            history.removeUndoStorage(oldest);
        }
    }

    private void evictOnDiskAsync(UUID playerUuid, int maxUndoRecords) {
        Path dir = undoHistoryDir(playerUuid);
        Set<UUID> disk = this.undoOnDisk.get(playerUuid);
        if (disk == null || disk.size() <= maxUndoRecords) return;
        MinecraftServer current = this.server;
        List<UUID> snapshot = new ArrayList<>(disk);
        this.ioExecutor.execute(() -> {
            try {
                List<UUID> sorted = snapshot.stream()
                        .sorted((a, b) -> Long.compare(this.readTime(undoPath(playerUuid, a)), this.readTime(undoPath(playerUuid, b))))
                        .toList();
                int toRemove = sorted.size() - maxUndoRecords;
                List<UUID> remove = new ArrayList<>();
                for (int i = 0; i < toRemove; i++) {
                    Path path = undoPath(playerUuid, sorted.get(i));
                    Files.deleteIfExists(path);
                    remove.add(sorted.get(i));
                }
                if (!remove.isEmpty()) {
                    current.execute(() -> {
                        Set<UUID> set = this.undoOnDisk.get(playerUuid);
                        if (set != null) {
                            remove.forEach(set::remove);
                            if (set.isEmpty()) this.undoOnDisk.remove(playerUuid);
                        }
                    });
                }
            } catch (IOException e) {
                LOGGER.error("Failed to evict undo records in {}", dir, e);
            }
        });
    }

    /** 该玩家实际生效的 undo 记录上限:个人配置与服务器配置的较小值,至少为 1 */
    private int effectiveMaxUndoRecords(ServerPlayerEntity player) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(player);
        int individual = info != null ? info.getCurrentIndividualConfig().getMaxUndoRecords() : 50;
        int server = this.serverConfig.getMaxUndoRecords();
        return Math.max(1, Math.min(individual, server));
    }

    private long readTime(Path path) {
        try {
            NbtCompound tag = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
            return tag.getLong("Time");
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private void scanUndoOnDisk(UUID playerUuid) {
        Path dir = undoHistoryDir(playerUuid);
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> stream = Files.list(dir)) {
            Set<UUID> uuids = stream
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .map(p -> p.getFileName().toString().replace(".dat", ""))
                    .map(u -> {
                        try {
                            return UUID.fromString(u);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            this.undoOnDisk.put(playerUuid, uuids);
        } catch (IOException e) {
            LOGGER.error("Failed to scan undo history of {}", playerUuid, e);
        }
    }

    private List<ItemStack> topStacks(UndoStorage storage) {
        return storage.getItems().values().stream()
                .sorted(Comparator.comparingInt(MaterialInfo::getMaxCount).reversed())
                .limit(3)
                .map(info -> {
                    ItemStack stack = info.getItemStack();
                    stack.setCount(1);
                    return stack;
                })
                .toList();
    }

    /** 从 undo 记录里随意挑一个方块坐标作为列表显示文本 */
    private static String undoPosText(UndoStorage storage) {
        if (storage.getMap().isEmpty()) {
            return "?";
        }
        long pos = storage.getMap().keySet().iterator().nextLong();
        int x = BlockPos.unpackLongX(pos);
        int y = BlockPos.unpackLongY(pos);
        int z = BlockPos.unpackLongZ(pos);
        return x + "," + y + "," + z;
    }

    private RegistryWrapper.WrapperLookup registryManager() {
        MinecraftServer current = this.server;
        if (current == null) throw new IllegalStateException("Server is not started");
        return current.getRegistryManager();
    }

    private void writeJson(Path path, String json) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, json);
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", path, e);
        }
    }

    private void writeUndo(Path path, NbtCompound tag) {
        try {
            Files.createDirectories(path.getParent());
            NbtIo.writeCompressed(tag, path);
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", path, e);
        }
    }
}
