package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.core.OnceMiningTask;
import com.wishtoday.ts.simpleminer.core.blockBreaker.BlockBreaker;

public class GlobalCondition {
    public static boolean isBlockBreaking() {
        return BlockBreaker.getBlockBreaking() || OnceMiningTask.isBlockBreaking();
    }
}
