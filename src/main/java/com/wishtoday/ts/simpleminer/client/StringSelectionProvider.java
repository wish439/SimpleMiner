package com.wishtoday.ts.simpleminer.client;

import lombok.SneakyThrows;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.List;

@Environment(EnvType.CLIENT)
public class StringSelectionProvider implements GuiProvider {
    @SneakyThrows
    @Override
    public List<AbstractConfigListEntry> get(String s, Field field
            , Object o, Object o1, GuiRegistryAccess guiRegistryAccess) {
        field.setAccessible(true);
        Object value = field.get(o);
        StringSelectionList annotation = field.getAnnotation(StringSelectionList.class);
        String[] strings = annotation.value();
        SelectionListEntry<Object> reset = new SelectionListEntry<>(Text.of(s), strings, value, Text.of("reset"), () -> {
            try {
                return field.get(o1);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, t -> {
            try {
                field.set(o, t);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, a -> {
            if (a instanceof String) {
                return Text.of((String) a);
            }
            return Text.of(a.toString());
        });
        return List.of(reset);
    }
}
