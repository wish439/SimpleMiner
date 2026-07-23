package com.wishtoday.ts.simpleminer.core.blockBreaker;

public interface ItemCollector {
    void start();
    boolean shouldCollectItem(CollectContext context);
    void collectItem(CollectContext context);
    CollectedResult finish();
    boolean shouldApplyMixin(CollectContext context);
}
