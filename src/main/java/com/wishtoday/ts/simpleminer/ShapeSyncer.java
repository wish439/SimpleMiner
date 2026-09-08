package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.network.OtherTextSyncS2CPayload;
import com.wishtoday.ts.simpleminer.network.RenderTextSyncS2CPayload;
import com.wishtoday.ts.simpleminer.shape.Shape;
import com.wishtoday.ts.simpleminer.shape.ShapeAdapter;
import com.wishtoday.ts.simpleminer.shape.ShapeAdapterManager;
import com.wishtoday.ts.simpleminer.shape.Shapes;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShapeSyncer {
    private final Shapes shapes;
    private final PressManager pressManager;
    private final ShapeAdapterManager manager;

    @CreateConstruction
    public ShapeSyncer(Shapes shapes, PressManager pressManager, ShapeAdapterManager manager) {
        this.shapes = shapes;
        this.pressManager = pressManager;
        this.manager = manager;
    }

    public void syncShapeNameTo(ServerPlayerEntity playerEntity) {
        List<Shape> list = shapes.sortedShapes();
        List<Text> texts = new ArrayList<>();
        for (Shape shape : list) {
            texts.add(shape.getDisplayName());
        }
        ServerPlayNetworking.send(playerEntity, new RenderTextSyncS2CPayload(texts));
    }

    public void syncShapeInfoTo(ServerPlayerEntity playerEntity) {
        PlayerMinerInfo info = this.pressManager.getPlayerMinerInfo(playerEntity);
        if (info == null) {
            return;
        }
        IndividualConfig config = info.getCurrentIndividualConfig();
        List<ShapeInfo> shapeInfos = new ArrayList<>();
        shapeInfos.add(config.getLinearShapeInfos());
        shapeInfos.add(config.getFullChunkShapeInfos());

        Int2ObjectOpenHashMap<List<Text>> map = new Int2ObjectOpenHashMap<>();

        for (ShapeInfo shapeInfo : shapeInfos) {
            int i = shapeInfo.shapeIndex();
            ShapeAdapter adapter = manager.getShapeAdapter(i);
            if (adapter == null) {
                continue;
            }
            List<Text> lines = adapter.getDisplayLines(config);
            map.put(i, lines);
        }
        ServerPlayNetworking.send(playerEntity, new OtherTextSyncS2CPayload(map));
    }
}
