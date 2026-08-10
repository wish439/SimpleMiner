package com.wishtoday.ts.simpleminer;

import com.wishtoday.ts.simpleminer.config.ServerConfig;

public interface Reloadable {
    boolean reload(ServerConfig config);
}
