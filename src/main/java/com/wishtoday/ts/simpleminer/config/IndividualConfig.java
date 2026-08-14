package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.ts.simpleminer.LinearShapeInfos;
import com.wishtoday.ts.simpleminer.client.SimpleminerClient;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class IndividualConfig {
    public static final PacketCodec<PacketByteBuf, IndividualConfig> CODEC = PacketCodec.of(IndividualConfig::write, IndividualConfig::read);

    private int personalMaxSize;

    private boolean toolPreventBroken;

    private LinearShapeInfos linearShapeInfos;

    public static List<Option<?>> getAllOptions(IndividualConfig config) {
        return List.of(personalMaxSize(config),
                toolPreventBroken(config));
    }

    public static List<OptionGroup> getAllGroups(IndividualConfig config) {
        return List.of(linearShapeInfos(config));
    }

    private static Option<Integer> personalMaxSize(IndividualConfig config) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable("simpleminer.config.individualConfig.personalMaxSize"))
                .description(OptionDescription.of(Text.translatable("simpleminer.config.individualConfig.personalMaxSize.description")))
                .binding(-1, config::getPersonalMaxSize, config::setPersonalMaxSize)
                .controller(integerOption -> IntegerFieldControllerBuilder
                        .create(integerOption)
                        .max(100000)
                        .min(-1)
                )
                .build();
    }

    private static Option<Boolean> toolPreventBroken(IndividualConfig config) {
        return Option.<Boolean>createBuilder()
                .name(Text.translatable("simpleminer.config.individualConfig.toolPreventBroken"))
                .description(OptionDescription.of(Text.translatable("simpleminer.config.toolPreventBroken.description")))
                .binding(false, config::isToolPreventBroken, config::setToolPreventBroken)
                .controller(b -> BooleanControllerBuilder.create(b)
                        .trueFalseFormatter())
                .build();
    }

    private static OptionGroup linearShapeInfos(IndividualConfig config) {
        int shapeIndex = SimpleminerClient.getShapeIndex();
        boolean b = shapeIndex == 1;
        return OptionGroup.createBuilder()
                .name(Text.translatable("simpleminer.config.individualConfig.linearShapeInfos"))
                .description(OptionDescription.of(Text.translatable("simpleminer.config.linearShapeInfos.description")))
                .option(Option.<Integer>createBuilder()
                        .name(Text.translatable("simpleminer.config.individualConfig.linearShapeInfos.height"))
                        .description(OptionDescription.of(Text.translatable("simpleminer.config.linearShapeInfos.description")))
                        .binding(1, config.getLinearShapeInfos()::getHeight, config.getLinearShapeInfos()::setHeight)
                        .available(b)
                        .controller(o -> IntegerFieldControllerBuilder.create(o).range(1, 1000))
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Text.translatable("simpleminer.config.individualConfig.linearShapeInfos.width"))
                        .description(OptionDescription.of(Text.translatable("simpleminer.config.linearShapeInfos.description")))
                        .binding(1, config.getLinearShapeInfos()::getWidth, config.getLinearShapeInfos()::setWidth)
                        .available(b)
                        .controller(o -> IntegerFieldControllerBuilder.create(o).range(1, 1000))
                        .build())
                .collapsed(true)
                .build();
    }


    public IndividualConfig() {
        this.personalMaxSize = -1;
        this.toolPreventBroken = false;
        this.linearShapeInfos = LinearShapeInfos.DEFAULT.copy();
    }


    private void write(PacketByteBuf buf) {
        buf.writeInt(this.personalMaxSize);
        buf.writeBoolean(this.toolPreventBroken);
        LinearShapeInfos.PACKET_CODEC.encode(buf, this.linearShapeInfos);
    }

    private static IndividualConfig read(PacketByteBuf buf) {
        int personalMaxSize1 = buf.readInt();
        boolean preventBroken = buf.readBoolean();
        LinearShapeInfos decode = LinearShapeInfos.PACKET_CODEC.decode(buf);
        return new IndividualConfig(personalMaxSize1, preventBroken, decode);
    }
}
