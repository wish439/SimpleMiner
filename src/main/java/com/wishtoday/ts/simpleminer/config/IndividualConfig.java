package com.wishtoday.ts.simpleminer.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;

import java.util.List;

@Setter
@Getter
public class IndividualConfig {
    public static final PacketCodec<PacketByteBuf, IndividualConfig> CODEC = PacketCodec.of(IndividualConfig::write, IndividualConfig::read);

    private int personalMaxSize;

    private boolean toolPreventBroken;

    public static List<Option<?>> getAllOptions(IndividualConfig config) {
        return List.of(personalMaxSize(config),
                toolPreventBroken(config));
    }

    public static List<OptionGroup> getAllGroups(IndividualConfig config) {
        return List.of();
    }

    private static Option<Integer> personalMaxSize(IndividualConfig config) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable("simpleminer.config.individualConfig.personalMaxSize"))
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
                .binding(false, config::isToolPreventBroken, config::setToolPreventBroken)
                .controller(b -> BooleanControllerBuilder.create(b)
                        .trueFalseFormatter())
                .build();
    }

    public IndividualConfig() {
        this.personalMaxSize = -1;
        this.toolPreventBroken = false;
    }


    private void write(PacketByteBuf buf) {
        buf.writeInt(this.personalMaxSize);
        buf.writeBoolean(this.toolPreventBroken);
    }

    private static IndividualConfig read(PacketByteBuf buf) {
        IndividualConfig config = new IndividualConfig();
        config.personalMaxSize = buf.readInt();
        config.toolPreventBroken = buf.readBoolean();
        return config;
    }
}
