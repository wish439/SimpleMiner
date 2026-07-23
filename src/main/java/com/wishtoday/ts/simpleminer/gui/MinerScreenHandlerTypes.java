package com.wishtoday.ts.simpleminer.gui;

import com.wishtoday.ts.simpleminer.undo.gui.screenHandler.UndoScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class MinerScreenHandlerTypes {

    public static final ScreenHandlerType<UndoScreenHandler> UNDO = register("undo", UndoScreenHandler::new);

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, Identifier.of("simpleminer", id), new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

    public static void init() {}
}
