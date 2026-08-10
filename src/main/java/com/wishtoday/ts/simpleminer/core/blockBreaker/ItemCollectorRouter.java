package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.DependOn;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Map;

@Service
@DependOn(ItemCollector.class)
public class ItemCollectorRouter implements Reloadable {
    @Delegate
    @Getter
    private volatile ItemCollector collector;
    private final Map<String, ItemCollector> delegates;
    private static final String DEFAULT_IMPLEMENTATION_KEY = "PUREAPI";
    private static ItemCollector defaultCollector;

    @CreateConstruction
    public ItemCollectorRouter(ServerConfig config, Map<String, ItemCollector> map) {
        this.delegates = map;
        this.reload(config);
    }

    @Override
    public boolean reload(ServerConfig config) {
        ItemCollector c = delegates.get(config.getCollectStrategy().toUpperCase());
        if (c != null) {
            this.collector = c;
            return true;
        }
        if (defaultCollector == null) {
            ItemCollector itemCollector = this.delegates.get(DEFAULT_IMPLEMENTATION_KEY);
            if (itemCollector == null) throw new IllegalStateException("No ItemCollector implementation defined");
            defaultCollector = itemCollector;
        }
        this.collector = defaultCollector;
        return false;
        //Optional<ItemCollector> first = Container.getInstance().getFirst(ItemCollector.class);
        //first.ifPresent(itemCollector -> this.collector = itemCollector);
    }

}
