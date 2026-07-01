package com.wishtoday.ts.simpleminer.client;

import lombok.SneakyThrows;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.List;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class IntegerFieldProvider implements GuiProvider {

    @SneakyThrows
    @Override
    public List<AbstractConfigListEntry> get(String s, Field field, Object o, Object defaults, GuiRegistryAccess guiRegistryAccess) {
        field.setAccessible(true);
        int value = field.getInt(o);
        IntegerListEntry reset = new IntegerListEntry(Text.of(s), value, Text.of("reset"), () -> {
            try {
                return field.getInt(defaults);
            } catch (IllegalAccessException e) {
                return -1;
            }
        }, i -> {
            try {
                field.setInt(o, i);
            } catch (IllegalAccessException e) {
            }
        });
        RangedIntegerField annotation = field.getAnnotation(RangedIntegerField.class);
        if (annotation != null) {
            reset.setMaximum(annotation.maxValue());
            reset.setMinimum(annotation.minValue());
        }
        return List.of(reset);
    }
}
