package com.wishtoday.ts.simpleminer.registry;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.ExtensionHandler;
import com.wishtoday.simpleservices.services.annotation.Service;

import java.util.Map;

@Service
public class RegistryExtender {
    private final Map<Class<?>, Registry<?>> registryMap;

    @CreateConstruction
    public RegistryExtender(@ExtensionHandler Map<Class<?>, Registry<?>> registryMap) {
        this.registryMap = registryMap;
    }

    public <T> void register(T object) {
        for (Registry<?> r : registryMap.values()) {
            if (r.handleClass().isInstance(object)) {
                Registry<T> registry = (Registry<T>) r;
                boolean register = (registry).register(object);
                if (register) {
                    registry.onRegistered(object);
                }
                return;
            }
        }
        throw new IllegalArgumentException("No registry for " + object.getClass());
    }
}
