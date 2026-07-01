package com.wishtoday.ts.simpleminer.client.gui;

import me.shedaniel.clothconfig2.gui.entries.AbstractNumberListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ToggleableIntegerInputEntry extends TooltipListEntry<Integer> {
    private IntegerSliderEntry integerSliderEntry;
    private IntegerListEntry integerRangeInputEntry;
    private ButtonWidget toggleButton;
    private boolean state;

    public ToggleableIntegerInputEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier) {
        super(fieldName, tooltipSupplier);
    }

    public ToggleableIntegerInputEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
    }

    public ToggleableIntegerInputEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier, IntegerSliderEntry integerSliderEntry, IntegerListEntry integerRangeInputEntry) {
        super(fieldName, tooltipSupplier);
        this.integerSliderEntry = integerSliderEntry;
        this.integerRangeInputEntry = integerRangeInputEntry;
        this.state = true;
        this.toggleButton = ButtonWidget
                .builder(Text.of("Toggle input mode"), a -> this.state = !this.state)
                .dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth("Toggle input mode") + 6, 20)
                .build();
    }

    @Override
    public Optional<Integer> getDefaultValue() {
        return state ? integerSliderEntry.getDefaultValue() : integerRangeInputEntry.getDefaultValue();
    }

    @Override
    public Integer getValue() {
        return state ? integerSliderEntry.getValue() : integerRangeInputEntry.getValue();
    }

    @Override
    public List<? extends Selectable> narratables() {
        return state ? integerSliderEntry.narratables() : integerRangeInputEntry.narratables();
    }

    @Override
    public List<? extends Element> children() {
        return state ? integerSliderEntry.children() : integerRangeInputEntry.children();
    }

    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.toggleButton.setX(x + entryWidth);
        if (state) {
            this.integerSliderEntry.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        } else {
            this.integerRangeInputEntry.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        }
    }
}
