package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
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

    @SerialEntry
    private int maxUndoRecords;

    @SerialEntry
    private List<String> testList;


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
        this.maxUndoRecords = 50;
        this.testList = new ArrayList();
    }



    public static List<Option<?>> getAllOptions(ServerConfig serverConfig) {
        return List.of(maxSize(serverConfig)
                , allowUndo(serverConfig)
                , maxUndoRecords(serverConfig)
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
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> maxUndoRecords(ServerConfig config) {
        return Option.<Integer>createBuilder()
                .name(Text.translatable("simpleminer.config.maxUndoRecords"))
                .description(OptionDescription.of(Text.translatable("simpleminer.config.maxUndoRecords.description")))
                .binding(50, config::getMaxUndoRecords, config::setMaxUndoRecords)
                .controller(integerOption -> IntegerFieldControllerBuilder
                        .create(integerOption)
                        .max(100000)
                        .min(1)
                )
                .build();
    }

    private static Option<String> collectStrategy(ServerConfig config) {
        return Option.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.collectStrategy"))
                .binding("PUREAPI", config::getCollectStrategy, config::setCollectStrategy)
                .controller(s -> CyclingListControllerBuilder.create(s).values("PUREAPI", "INTERCEPT").formatValue(Text::of)).build();
    }

    private static Option<String> blockBreakStrategy(ServerConfig config) {
        return Option.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.blockBreakStrategy"))
                .binding("PUREAPI", config::getBlockBreakStrategy, config::setBlockBreakStrategy)
                .controller(s -> CyclingListControllerBuilder.create(s).values("PUREAPI", "VANILLA").formatValue(Text::of))
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
/*
        return ListOption.<String>createBuilder()
                .name(Text.translatable("simpleminer.config.blockFamilies"))
                .binding(List.of("#minecraft:base_stone_overworld"), config::getBlockFamilies, config::setBlockFamilies)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
*/


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
        buf.writeVarInt(this.maxUndoRecords);
    }

    private static ServerConfig read(PacketByteBuf buf) {
        int i = buf.readVarInt();
        boolean b = buf.readBoolean();
        String s = buf.readString();
        String string = buf.readString();
        String readString = buf.readString();
        ArrayList<String> strings = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        ArrayList<String> strings1 = buf.readCollection(ArrayList::new, PacketByteBuf::readString);
        int maxUndoRecords = buf.readVarInt();
        return new ServerConfig(i, b, s, string, readString, strings, strings1, maxUndoRecords, null);
    }

    public void setFromConfig(ServerConfig config) {
        this.maxSize = config.maxSize;
        this.allowUndo = config.allowUndo;
        this.collectStrategy = config.collectStrategy;
        this.blockBreakStrategy = config.blockBreakStrategy;
        this.blockFamilies = config.blockFamilies;
        this.rightClickHandler = config.rightClickHandler;
        this.supportCrops = config.supportCrops;
        this.maxUndoRecords = config.maxUndoRecords;
    }
}
