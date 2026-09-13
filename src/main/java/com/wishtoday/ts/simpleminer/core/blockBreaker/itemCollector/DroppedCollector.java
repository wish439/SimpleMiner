package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;

public interface DroppedCollector {
    void start();

    boolean shouldCollect(CollectContext context);

    void collectItem(CollectContext context);

    CollectedResult finish();

    boolean shouldApplyMixin(CollectContext context, String mixinName);

    void collectExperience(CollectContext context);
}
