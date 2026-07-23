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

    private boolean allowUndo;

    public ServerConfig() {
        this.maxSize = 64;
        this.allowUndo = false;
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.maxSize);
        buf.writeBoolean(this.allowUndo);
    }

    private static ServerConfig read(PacketByteBuf buf) {
        ServerConfig config = new ServerConfig();
        config.maxSize = buf.readVarInt();
        config.allowUndo = buf.readBoolean();
        return config;
    }

    public void setFromConfig(ServerConfig config) {
        this.maxSize = config.maxSize;
        this.allowUndo = config.allowUndo;
    }
}
