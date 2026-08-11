package com.wishtoday.ts.simpleminer.core.rightClick;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import lombok.experimental.Delegate;

import java.util.Map;

@Service
public class RightClickHandlerRouter implements Reloadable {
    @Delegate
    private RightClickHandler handler;
    private final Map<String, RightClickHandler> delegates;
    private static final String DEFAULT_IMPLEMENTATION_KEY = "VANILLA";
    private static RightClickHandler defaultCollector;
    @CreateConstruction
    public RightClickHandlerRouter(ServerConfig serverConfig, Map<String, RightClickHandler> handlers) {
        this.delegates = handlers;
        this.reload(serverConfig);
    }

    @Override
    public boolean reload(ServerConfig config) {
        RightClickHandler c = delegates.get(config.getRightClickHandler().toUpperCase());
        if (c != null) {
            this.handler = c;
            return true;
        }
        if (defaultCollector == null) {
            RightClickHandler itemCollector = this.delegates.get(DEFAULT_IMPLEMENTATION_KEY);
            if (itemCollector == null) throw new IllegalStateException("No ItemCollector implementation defined");
            defaultCollector = itemCollector;
        }
        this.handler = defaultCollector;
        return false;
    }
}
