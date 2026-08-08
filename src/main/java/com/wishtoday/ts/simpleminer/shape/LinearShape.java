package com.wishtoday.ts.simpleminer.shape;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Service
public class LinearShape implements Shape {
    @CreateConstruction
    public LinearShape() {
    }

    @Override
    public LongOpenHashSet walk(ShapeContext context) {
        Direction facing = context.getDirection();
        BlockPos currentTargetPos = context.getCurrentTargetPos();
        long targetPosLong = currentTargetPos.asLong();
        LongOpenHashSet longs = LongOpenHashSet.of(targetPosLong);
        while (context.getMaxSize() > longs.size()) {
            long add = BlockPos.add(targetPosLong, facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ());
            targetPosLong = add;
            longs.add(add);
        }
        return longs;
    }

    @Override
    public int index() {
        return 1;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("线形");
    }
}
