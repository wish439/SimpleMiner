package com.wishtoday.ts.simpleminer.lifecycle;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class LifecycleRegistry {
    private final List<Lifecycle> initializers;

    @CreateConstruction
    public LifecycleRegistry(List<Lifecycle> initializers) {
        this.initializers = initializers;
    }

    public void accept(Consumer<Lifecycle> consumer) {
        for (Lifecycle initializer : initializers) {
            consumer.accept(initializer);
        }
    }
}
