package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.DependOn;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Map;

@Service
@DependOn(DroppedCollector.class)
public class ItemCollectorRouter implements Reloadable {
    @Delegate
    @Getter
    private volatile DroppedCollector collector;
    private final Map<String, DroppedCollector> delegates;
    private static final String DEFAULT_IMPLEMENTATION_KEY = "PUREAPI";
    private static DroppedCollector defaultCollector;

    @CreateConstruction
    public ItemCollectorRouter(ServerConfig config, Map<String, DroppedCollector> map) {
        this.delegates = map;
        this.reload(config);
    }

    @Override
    public boolean reload(ServerConfig config) {
        DroppedCollector c = delegates.get(config.getCollectStrategy().toUpperCase());
        if (c != null) {
            this.collector = c;
            return true;
        }
        if (defaultCollector == null) {
            DroppedCollector itemCollector = this.delegates.get(DEFAULT_IMPLEMENTATION_KEY);
            if (itemCollector == null) throw new IllegalStateException("No DroppedCollector implementation defined");
            defaultCollector = itemCollector;
        }
        this.collector = defaultCollector;
        return false;
        //Optional<DroppedCollector> first = Container.getInstance().getFirst(DroppedCollector.class);
        //first.ifPresent(itemCollector -> this.collector = itemCollector);
    }

}
