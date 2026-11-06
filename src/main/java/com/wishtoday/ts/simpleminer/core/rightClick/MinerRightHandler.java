package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ControllerManager;
import com.wishtoday.ts.simpleminer.core.SimpleItemStackCollector;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.core.ShapeAnalyzer;
import com.wishtoday.ts.simpleminer.shape.ShapeRefresher;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollectorResult;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemDropper;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Service
public class MinerRightHandler {
    private final ControllerManager pressManager;
    private final ShapeRefresher shapeRefresher;
    private final ShapeAnalyzer shapeAnalyzer;
    private final RightClickHandlerRouter rightHandler;
    private final ItemDropper dropper;
    private static final ThreadLocal<Boolean> handlingRightClick = ThreadLocal.withInitial(() -> false);

    @CreateConstruction
    public MinerRightHandler(ControllerManager pressManager, ShapeRefresher shapeRefresher, ShapeAnalyzer shapeAnalyzer, RightClickHandlerRouter rightHandler, ItemDropper dropper) {
        this.pressManager = pressManager;
        this.shapeRefresher = shapeRefresher;
        this.shapeAnalyzer = shapeAnalyzer;
        this.rightHandler = rightHandler;
        this.dropper = dropper;
    }

    public ActionResult handleRightClick(ServerPlayerEntity player
            , World world
            , Hand hand
            , BlockHitResult hitResult) {
        if (handlingRightClick.get()) {
            return ActionResult.PASS;
        }
        BlockPos pos = hitResult.getBlockPos();
        MinerShapeInfo info = this.pressManager.getPressedMinerShapeInfo(player);
        if (info == null) return ActionResult.PASS;
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return ActionResult.PASS;
        LongList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        MutableBlockHitResult mutableBlockHitResult = new MutableBlockHitResult(hitResult.getPos(), hitResult.getSide(), hitResult.getBlockPos(), hitResult.isInsideBlock());
        boolean success = false;
        ItemStackCollector collector = new SimpleItemStackCollector();
        for (long sortedBlockPose : sortedBlockPoses) {
            mutable.set(sortedBlockPose);
            mutableBlockHitResult.setBlockPos(mutable);
            ActionResult result = this.tryRightClick(player, world, hand, mutableBlockHitResult, collector);
            if (result == ActionResult.SUCCESS) success = true;
        }

        ItemCollectorResult result = collector.toResult();
        this.dropper.dropStack(world, pos, result.getMap());
        return success ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    private ActionResult tryRightClick(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult, ItemStackCollector collector) {
        try {
            handlingRightClick.set(true);
            return rightHandler.onUse(player, world, hand, hitResult, collector);
        } finally {
            handlingRightClick.set(false);
        }
    }
}
