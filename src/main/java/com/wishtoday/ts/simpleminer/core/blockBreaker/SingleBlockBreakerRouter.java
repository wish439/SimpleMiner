package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Router;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Map;

@Service
public class SingleBlockBreakerRouter implements Router {

    private final Map<String, SingleBlockBreaker> delegates;
    private static final String DEFAULT_IMPLEMENTATION_KEY = "PUREAPI";
    private static SingleBlockBreaker defaultBreaker;
    @Delegate
    @Getter
    private SingleBlockBreaker blockBreaker;

    @CreateConstruction
    public SingleBlockBreakerRouter(ServerConfig config, Map<String, SingleBlockBreaker> map) {
        this.delegates = map;
        this.reload(config);
    }
    @Override
    public boolean reload(ServerConfig config) {
        SingleBlockBreaker singleBlockBreaker = this.delegates.get(config.getBlockBreakStrategy());
        if (singleBlockBreaker != null) {
            this.blockBreaker = singleBlockBreaker;
            return true;
        }
        if (defaultBreaker == null) {
            SingleBlockBreaker singleBlockBreaker1 = this.delegates.get(DEFAULT_IMPLEMENTATION_KEY);
            if (singleBlockBreaker1 == null) throw new IllegalStateException("Default implementation key has not been set");
            defaultBreaker = singleBlockBreaker1;
        }
        this.blockBreaker = defaultBreaker;

        return false;
    }
}
