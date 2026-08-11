package com.wishtoday.ts.simpleminer.core.rightClick;

import lombok.Setter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Setter
public class MutableBlockHitResult extends BlockHitResult {
    private Direction side;
    private BlockPos blockPos;
    private boolean missed;
    private boolean insideBlock;
    public MutableBlockHitResult(Vec3d pos, Direction side, BlockPos blockPos, boolean insideBlock) {
        super(pos, side, blockPos, insideBlock);
        this.blockPos = blockPos;
        this.side = side;
        this.missed = false;
        this.insideBlock = insideBlock;
    }
    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public Direction getSide() {
        return this.side;
    }

    @Override
    public Type getType() {
        return super.getType();
    }

    @Override
    public Vec3d getPos() {
        return super.getPos();
    }
}
