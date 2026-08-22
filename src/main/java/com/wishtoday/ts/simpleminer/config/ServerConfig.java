package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import dev.isxander.yacl3.api.Option;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Getter
@Service
@Setter
@AllArgsConstructor
public class ServerConfig {
    public static final PacketCodec<PacketByteBuf, ServerConfig> CODEC = PacketCodec.of(ServerConfig::write, ServerConfig::read);

    //@ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
    @SerialEntry
    private int maxSize;

    @SerialEntry
    private boolean allowUndo;

    @SerialEntry
    private String collectStrategy;

    @SerialEntry
    private String blockBreakStrategy;

    @SerialEntry
    private String rightClickHandler;

    @SerialEntry
    private List<String> blockFamilies;

    @SerialEntry
    private List<String> supportCrops;

    @CreateConstruction
    public ServerConfig() {
        this.maxSize = 64;
        this.allowUndo = false;
        this.collectStrategy = "PUREAPI";
        this.blockBreakStrategy = "PUREAPI";
        this.rightClickHandler = "NOBLOCKITEM";
        this.blockFamilies = List.of("#minecraft:base_stone_overworld", "minecraft:coal_ore,minecraft:deepslate_coal_ore",
                "minecraft:iron_ore,minecraft:deepslate_iron_ore",
                "minecraft:copper_ore,minecraft:deepslate_copper_ore",
                "minecraft:gold_ore,minecraft:deepslate_gold_ore",
                "minecraft:redstone_ore,minecraft:deepslate_redstone_ore",
                "minecraft:emerald_ore,minecraft:deepslate_emerald_ore",
                "minecraft:lapis_ore,minecraft:deepslate_lapis_ore",
                "minecraft:diamond_ore,minecraft:deepslate_diamond_ore");
        this.supportCrops = List.of("#minecraft:crops");
    }



    public static List<Option<?>> getAllOptions(ServerConfig serverConfig) {
        return List.of(maxSize(serverConfig)
                , allowUndo(serverConfig)
                , collectStrategy(serverConfig)
                , blockBreakStrategy(serverConfig)
                , rightClickHandler(serverConfig));
    }

    public static List<OptionGroup> getAllGroups(ServerConfig serverConfig) {
        return List.of(blockFamilies(serverConfig)
                , supportCrops(serverConfig));
    }

    private static Option<Integer> maxSize(ServerConfig config) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable("simpleminer.config.maxSize"))
                .binding(64, config::getMaxSize, config::setMaxSize)
                .controller(integerOption -> IntegerFieldControllerBuilder
                        .create(integerOption)
                        .max(Integer.MAX_VALUE)
                        .min(1)
                )
                .build();
    }

    private static Option<Boolean> allowUndo(ServerConfig config) {
        return Option.<Boolean>createBuilder()
                .name(Text.translatable("simpleminer.config.allowUndo"))
                .binding(false, config::isAllowUndo, config::setAllowUndo)
                .controller(b -> BooleanControllerBuilder.create(b)
                        .trueFalseFormatter())
                .build();
    }

    private static Option<String> collectStrategy(ServerConfig config) {
        return Option.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.collectStrategy"))
                .binding("PUREAPI", config::getCollectStrategy, config::setCollectStrategy)
                .controller(s -> DropdownStringControllerBuilder.create(s).values("PUREAPI", "INTERCEPT")
                ).build();
    }

    private static Option<String> blockBreakStrategy(ServerConfig config) {
        return Option.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.blockBreakStrategy"))
                .binding("PUREAPI", config::getBlockBreakStrategy, config::setBlockBreakStrategy)
                .controller(s -> DropdownStringControllerBuilder.create(s).values("PUREAPI", "VANILLA"))
                .build();
    }

    private static Option<String> rightClickHandler(ServerConfig config) {
        return Option.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.rightClickHandler"))
                .binding("NOBLOCKITEM", config::getRightClickHandler, config::setRightClickHandler)
                .controller(s -> DropdownStringControllerBuilder.create(s).values("VANILLA", "NOBLOCKITEM"))
                .build();
    }

    private static OptionGroup blockFamilies(ServerConfig config) {
        return ListOption.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.blockFamilies"))
                .binding(List.of("#minecraft:base_stone_overworld"), config::getBlockFamilies, config::setBlockFamilies)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }

    private static OptionGroup supportCrops(ServerConfig config) {
        return ListOption.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.supportCrops"))
                .binding(List.of("#minecraft:crops"), config::getSupportCrops, config::setSupportCrops)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.maxSize);
        buf.writeBoolean(this.allowUndo);
        buf.writeString(this.collectStrategy);
        buf.writeString(this.blockBreakStrategy);
        buf.writeString(this.rightClickHandler);
        buf.writeCollection(this.blockFamilies, PacketByteBuf::writeString);
        buf.writeCollection(this.supportCrops, PacketByteBuf::writeString);
    }

    private static ServerConfig read(PacketByteBuf buf) {
        int i = buf.readVarInt();
        boolean b = buf.readBoolean();
        String s = buf.readString();
        String string = buf.readString();
        String readString = buf.readString();
        ArrayList<String> strings = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        ArrayList<String> strings1 = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        return new ServerConfig(i, b, s, string, readString, strings, strings1);
    }

    public void setFromConfig(ServerConfig config) {
        this.maxSize = config.maxSize;
        this.allowUndo = config.allowUndo;
        this.collectStrategy = config.collectStrategy;
        this.blockBreakStrategy = config.blockBreakStrategy;
        this.blockFamilies = config.blockFamilies;
        this.rightClickHandler = config.rightClickHandler;
        this.supportCrops = config.supportCrops;
    }
}
