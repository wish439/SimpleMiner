package com.wishtoday.ts.simpleminer.client;

import com.wishtoday.simpleservices.services.annotation.PostConstruct;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.ConfigType;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import com.wishtoday.ts.simpleminer.network.config.OpenConfigS2CPayload;
import com.wishtoday.ts.simpleminer.network.config.SyncConfigC2SPayload;
import com.wishtoday.ts.simpleminer.services.ClientOnlyLoadCondition;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

@Service(condition = ClientOnlyLoadCondition.class)
public class ClientNetworkingRegistry {

    @PostConstruct
    public void registerChannels() {
        PayloadTypeRegistry.playS2C().register(OpenConfigS2CPayload.ID, OpenConfigS2CPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpenConfigS2CPayload.ID, this::receiveOpenConfigPayload);
    }

    private void receiveOpenConfigPayload(OpenConfigS2CPayload payload, ClientPlayNetworking.Context context) {
        ConfigType type = payload.type();
        if (type == ConfigType.SERVER) {
            openConfigScreen(ConfigType.SERVER, (ServerConfig) payload.currentConfig(), ServerConfig.class);
        }
        if (type == ConfigType.INDIVIDUAL) {
            openConfigScreen(ConfigType.INDIVIDUAL, (IndividualConfig) payload.currentConfig(), IndividualConfig.class);
        }
    }

    private <T extends ConfigData> void openConfigScreen(ConfigType type, T config, Class<T> configClass) {
        ConfigHolder<T> holder = AutoConfig.getConfigHolder(configClass);

        holder.registerSaveListener((holderObj, newConfig) -> {
            ClientPlayNetworking.send(new SyncConfigC2SPayload(type, newConfig));
            return ActionResult.SUCCESS;
        });
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(configClass);
        guiRegistry.registerAnnotationProvider(new IntegerFieldProvider(), RangedIntegerField.class);

        holder.setConfig(config);

        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(AutoConfig.getConfigScreen(configClass, mc.currentScreen).get());
    }
}
