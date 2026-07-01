package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.client.RangedIntegerField;
import lombok.Getter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

@Getter
@Service
@Config(name = "simpleminer/individual")
public class IndividualConfig implements ConfigData {
    public static final PacketCodec<PacketByteBuf, IndividualConfig> CODEC = PacketCodec.of(IndividualConfig::write, IndividualConfig::read);

    @RangedIntegerField(minValue = -1, maxValue = 100000)
    private int personalMaxSize;

    private boolean toolPreventBroken;

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
