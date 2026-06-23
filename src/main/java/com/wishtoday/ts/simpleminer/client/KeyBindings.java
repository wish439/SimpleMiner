package com.wishtoday.ts.simpleminer.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding MINE_KEY;

    public static void register() {
        MINE_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.simpleminer.mine",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_GRAVE_ACCENT,
                        "simpleminer.category"
                )
        );
    }
}
