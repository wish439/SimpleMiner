package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.*;
import com.wishtoday.ts.simpleminer.network.KeywordPressedPayload;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeContext;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import com.wishtoday.ts.simpleminer.utils.WorldUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class Simpleminer implements ModInitializer {

    @Override
    public void onInitialize() {
        ServiceAssembler assembler = new ServiceAssembler("com.wishtoday.ts.simpleminer");
        assembler.registerFactoryExtension(new CollectionExtension());
        assembler.registerFactoryExtension(new ExtensionHandlerMapExtension());
        assembler.registerFactoryExtension(new QualifierExtension());
        assembler.registerFactoryExtension(new TwoTypeMapExtension());
        assembler.distributeDepends();
    }
}
