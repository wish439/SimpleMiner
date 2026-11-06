package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.ts.simpleminer.core.ItemStackCollector;
import com.wishtoday.ts.simpleminer.core.blockBreaker.CollectContext;
import com.wishtoday.ts.simpleminer.core.blockBreaker.ExperienceCollector;

public interface ExternalStorageAdvocatedCollector {
    boolean shouldCollect(CollectContext context);

    void collectItem(CollectContext context, ItemStackCollector collector);

    void collectExperience(CollectContext context, ExperienceCollector collector);
}
