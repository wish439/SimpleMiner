package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.DependOn;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Map;

@Service
@DependOn(ExternalStorageAdvocatedCollector.class)
public class ExternalStorageAdvocatedCollectorRouter implements Reloadable {
    @Delegate
    @Getter
    private volatile ExternalStorageAdvocatedCollector collector;
    private final Map<String, ExternalStorageAdvocatedCollector> delegates;
    private static final String DEFAULT_IMPLEMENTATION_KEY = "PUREAPI";
    private static ExternalStorageAdvocatedCollector defaultCollector;

    public ExternalStorageAdvocatedCollectorRouter(ServerConfig config,Map<String, ExternalStorageAdvocatedCollector> delegates) {
        this.delegates = delegates;
        this.reload(config);
    }

    @Override
    public boolean reload(ServerConfig config) {
        ExternalStorageAdvocatedCollector c = delegates.get(config.getCollectStrategy().toUpperCase());
        if (c != null) {
            this.collector = c;
            return true;
        }
        if (defaultCollector == null) {
            ExternalStorageAdvocatedCollector itemCollector = this.delegates.get(DEFAULT_IMPLEMENTATION_KEY);
            if (itemCollector == null) throw new IllegalStateException("No ExternalStorageAdvocatedCollector implementation defined");
            defaultCollector = itemCollector;
        }
        this.collector = defaultCollector;
        return false;
    }
}
