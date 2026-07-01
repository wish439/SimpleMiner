package com.wishtoday.ts.simpleminer.config;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.client.RangedIntegerField;
import lombok.Getter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

@Getter
@Service
@Config(name = "simpleminer/server")
public class ServerConfig implements ConfigData {
    public static final PacketCodec<PacketByteBuf, ServerConfig> CODEC = PacketCodec.of(ServerConfig::write, ServerConfig::read);

    @RangedIntegerField(minValue = 1, maxValue = 100000)
    //@ConfigEntry.BoundedDiscrete(min = 1, max = 100000)
    private int maxSize;

    public ServerConfig() {
        this.maxSize = 64;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.maxSize);
    }

    private static ServerConfig read(PacketByteBuf buf) {
        ServerConfig config = new ServerConfig();
        config.maxSize = buf.readVarInt();
        return config;
    }

    public void setFromConfig(ServerConfig config) {
        this.maxSize = config.maxSize;
    }
}
