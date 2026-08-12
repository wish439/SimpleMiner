package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.PressManager;
import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.core.ShapeAnalyzer;
import com.wishtoday.ts.simpleminer.core.ShapeRefresher;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ItemDropper;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Service
public class MinerRightHandler {
    private final PressManager pressManager;
    private final ShapeRefresher shapeRefresher;
    private final ShapeAnalyzer shapeAnalyzer;
    private final RightClickHandlerRouter rightHandler;
    private final ItemDropper dropper;
    private static final ThreadLocal<Boolean> handlingRightClick = ThreadLocal.withInitial(() -> false);

    @CreateConstruction
    public MinerRightHandler(PressManager pressManager, ShapeRefresher shapeRefresher, ShapeAnalyzer shapeAnalyzer, RightClickHandlerRouter rightHandler, ItemDropper dropper) {
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
        PlayerMinerInfo info = this.pressManager.getPressedPlayerMinerInfo(player);
        if (info == null) return ActionResult.PASS;
        this.shapeRefresher.refresh(info, pos);
        ShapeResult shapeResult = info.getBlockPoses();
        if (shapeResult == null) return ActionResult.PASS;
        LongArrayList sortedBlockPoses = shapeResult.getSortedBlockPoses();
        LongOpenHashSet internal = shapeAnalyzer.calcCompleteSurrounded(shapeResult.getBlockPoses());

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        MutableBlockHitResult mutableBlockHitResult = new MutableBlockHitResult(hitResult.getPos(), hitResult.getSide(), hitResult.getBlockPos(), hitResult.isInsideBlock());
        boolean success = false;
        ItemStackCollector collector = new ItemStackCollector();
        for (long sortedBlockPose : sortedBlockPoses) {
            mutable.set(sortedBlockPose);
            mutableBlockHitResult.setBlockPos(mutable);
            handlingRightClick.set(true);
            ActionResult result = rightHandler.onUse(player, world, hand, mutableBlockHitResult, !internal.contains(sortedBlockPose), collector);
            handlingRightClick.set(false);
            if (result == ActionResult.SUCCESS) success = true;
        }

        CollectedResult result = collector.toResult();
        this.dropper.dropStack(world, pos, result);
        return success ? ActionResult.SUCCESS : ActionResult.PASS;
    }
}
