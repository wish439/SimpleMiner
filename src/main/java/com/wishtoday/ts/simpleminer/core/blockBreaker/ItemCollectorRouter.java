package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Router;
import lombok.experimental.Delegate;

@Service
public class ItemCollectorRouter implements Router {
    @Delegate
    private ItemCollector collector;

    @CreateConstruction
    public ItemCollectorRouter(ItemCollector collector) {
        this.collector = collector;
    }

    //public void start() {
    //    this.collector.start();
    //}
    //public boolean shouldCollectItem(CollectContext context) {
    //    return this.collector.shouldCollectItem(context);
    //}
    //public void collectItem(CollectContext context) {
    //    this.collector.collectItem(context);
    //}
    //public CollectedResult finish() {
    //    return this.collector.finish();
    //}

    @Override
    public void reload() {

    }
}
