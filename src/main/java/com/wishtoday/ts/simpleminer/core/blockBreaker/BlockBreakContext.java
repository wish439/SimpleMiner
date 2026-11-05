package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.ts.simpleminer.config.IndividualConfig;
import com.wishtoday.ts.simpleminer.data.MinerShapeInfo;
import com.wishtoday.ts.simpleminer.data.PlayerMinerInfo;
import com.wishtoday.ts.simpleminer.shape.ShapeResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class BlockBreakContext {
    private final World world;
    private final PlayerEntity player;
    private final BlockPos originPos;
    private final BlockState originState;
    private final @Nullable BlockEntity blockEntity;
    private final MinerShapeInfo info;
    private final IndividualConfig config;
    private final ShapeResult shapeResult;
    private final ItemStack mainHandStack;
    private BlockPos currentPos;
    private BlockState currentState;
    private long currentBlockPos;

    public BlockBreakContext(World world, PlayerEntity player, BlockPos originPos, BlockState originState, @Nullable BlockEntity blockEntity, MinerShapeInfo info, IndividualConfig config, ShapeResult shapeResult, ItemStack mainHandStack) {
        this.world = world;
        this.player = player;
        this.originPos = originPos;
        this.originState = originState;
        this.blockEntity = blockEntity;
        this.info = info;
        this.config = config;
        this.shapeResult = shapeResult;
        this.mainHandStack = mainHandStack;
        this.currentPos = originPos;
        this.currentState = originState;
        this.currentBlockPos = originPos.asLong();
    }

    public void setCurrentPos(BlockPos currentPos) {
        this.currentPos = currentPos;
        this.currentBlockPos = currentPos.asLong();
    }
}
