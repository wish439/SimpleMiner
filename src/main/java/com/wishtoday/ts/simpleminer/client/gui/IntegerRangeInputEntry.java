package com.wishtoday.ts.simpleminer.client.gui;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Environment(net.fabricmc.api.EnvType.CLIENT)
public class IntegerRangeInputEntry extends TooltipListEntry<Integer> {
    private int minimum;
    private int maximum;
    private TextFieldWidget textFieldWidget;
    public IntegerRangeInputEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, tooltipSupplier, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    public IntegerRangeInputEntry(Text fieldName, @Nullable Supplier<Optional<Text[]>> tooltipSupplier, int minimum, int maximum) {
        super(fieldName, tooltipSupplier);
        this.minimum = minimum;
        this.maximum = maximum;
        this.textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 148, 18, Text.empty()) {

        };
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public List<? extends Selectable> narratables() {
        return List.of();
    }

    @Override
    public Integer getValue() {
        return 0;
    }

    @Override
    public Optional<Integer> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
    }
}
