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
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
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
            openConfigScreen(ConfigType.SERVER, (ServerConfig) payload.currentConfig());
        }
        if (type == ConfigType.INDIVIDUAL) {
            openConfigScreen(ConfigType.INDIVIDUAL, (IndividualConfig) payload.currentConfig());
        }
    }

    private <T> void openConfigScreen(ConfigType configType, T config) {
        String translationKey = configType == ConfigType.SERVER ? "simpleminer.config.serverConfig" : "simpleminer.config.individualConfig";
        List<Option<?>> options = configType == ConfigType.SERVER ? ServerConfig.getAllOptions((ServerConfig) config) : IndividualConfig.getAllOptions((IndividualConfig) config);
        List<OptionGroup> groups = configType == ConfigType.SERVER ? ServerConfig.getAllGroups((ServerConfig) config) : IndividualConfig.getAllGroups((IndividualConfig) config);
        YetAnotherConfigLib build = YetAnotherConfigLib.createBuilder()
                .title(Text.translatable(translationKey))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable(translationKey))
                        .options(options)
                        .groups(groups)
                        .build())
                .save(() -> ClientPlayNetworking.send(new SyncConfigC2SPayload(configType, config)))
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
