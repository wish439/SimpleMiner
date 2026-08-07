package com.wishtoday.ts.simpleminer.core.blockBreaker;

public interface MixinDependCollector {
    default boolean shouldCollectItemFromMixin(CollectContext context, String mixinName) {
        return false;
    }
    default void collectItemFromMixin(CollectContext context, String mixinName) {
    }
}
