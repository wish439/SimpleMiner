package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.core.OnceMiningTask;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;
import lombok.Setter;

public class GlobalCondition {
    @Setter
    private static boolean blockBreaking = false;
    public static boolean isBlockBreaking() {
        return BlockBreaker.getBlockBreaking() || OnceMiningTask.isBlockBreaking() || blockBreaking;
    }
}
