package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.MineBlockSyncS2CPayload;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientNetworkingRegistry {

    private final List<ClientNetworkExtendFutures> futures;

    @CreateConstruction
    public ClientNetworkingRegistry(List<ClientNetworkExtendFutures> futures) {
        this.futures = futures;
    }

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playS2C().register(OpenConfigS2CPayload.ID, OpenConfigS2CPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpenConfigS2CPayload.ID, this::receiveOpenConfigPayload);

        PayloadTypeRegistry.playS2C().register(MineBlockSyncS2CPayload.ID, MineBlockSyncS2CPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(MineBlockSyncS2CPayload.ID, this::receiveUndoDataSyncPayload);

        this.futures.forEach(ClientNetworkExtendFutures::initialize);
    }

    private void receiveUndoDataSyncPayload(MineBlockSyncS2CPayload payload, ClientPlayNetworking.Context context) {
        SimpleminerClient.setCurrentBlocks(payload.length());
    }

    private void receiveOpenConfigPayload(OpenConfigS2CPayload payload, ClientPlayNetworking.Context context) {
        ConfigType type = payload.type();
        if (type == ConfigType.SERVER) {
            openServerConfigScreen((ServerConfig) payload.currentConfig());
        }
        if (type == ConfigType.INDIVIDUAL) {
            openIndividualConfigScreen((IndividualConfig) payload.currentConfig());
        }
    }

    private void openServerConfigScreen(ServerConfig serverConfig) {
        String translationKey = "simpleminer.config.serverConfig";
        List<Option<?>> options = ServerConfig.getAllOptions(serverConfig);
        List<OptionGroup> groups = ServerConfig.getAllGroups(serverConfig);
        YetAnotherConfigLib build = YetAnotherConfigLib.createBuilder()
                .title(Text.translatable(translationKey))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable(translationKey))
                        .options(options)
                        .groups(groups)
                        .build())
                .save(() -> ClientPlayNetworking.send(new SyncConfigC2SPayload(ConfigType.SERVER, serverConfig)))
                .build();
        MinecraftClient mc = MinecraftClient.getInstance();
        Screen screen = build.generateScreen(mc.currentScreen);
        mc.setScreen(screen);
    }

    private <T> void openIndividualConfigScreen(IndividualConfig config) {
        String translationKey = "simpleminer.config.individualConfig";
        List<Option<?>> options = IndividualConfig.getAllOptions(config);
        List<OptionGroup> groups = IndividualConfig.getAllGroups(config, SimpleminerClient.getShapeIndex());
        ConfigCategory category = ConfigCategory.createBuilder()
                .name(Text.translatable(translationKey))
                .groups(groups)
                .options(options)
                .build();
        YetAnotherConfigLib build = YetAnotherConfigLib.createBuilder()
                .title(Text.translatable(translationKey))
                .category(category)
                .save(() -> {
                    ClientPlayNetworking.send(new SyncConfigC2SPayload(ConfigType.INDIVIDUAL, config));
                    SimpleminerClient.consumeIndividualConfig(config);
                })
                .build();
        MinecraftClient mc = MinecraftClient.getInstance();
        Screen screen = build.generateScreen(mc.currentScreen);
        mc.setScreen(screen);
        /*ConfigHolder<T> holder = AutoConfig.getConfigHolder(configClass);

        holder.registerSaveListener((holderObj, newConfig) -> {
            ClientPlayNetworking.send(new SyncConfigC2SPayload(type, newConfig));
            return ActionResult.SUCCESS;
        });
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(configClass);
        guiRegistry.registerAnnotationProvider(new IntegerFieldProvider(), RangedIntegerField.class);
        guiRegistry.registerAnnotationProvider(new StringSelectionProvider(), StringSelectionList.class);

        holder.setConfig(config);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(AutoConfig.getConfigScreen(configClass, mc.currentScreen).get());*/
    }
}
