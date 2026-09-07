package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectedResult;

public interface ItemCollector {
    void start();

    boolean shouldCollectItem(CollectContext context);

    void collectItem(CollectContext context);

    CollectedResult finish();

    boolean shouldApplyMixin(CollectContext context, String mixinName);
}
