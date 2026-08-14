package com.wishtoday.ts.simpleminer.config;

import dev.isxander.yacl3.api.Option;

import java.util.function.Supplier;

public record ConditionOption<T>(Option<T> option, Supplier<Boolean> condition, boolean isTrue) {
    public ConditionOption(Option<T> option, Supplier<Boolean> condition) {
        this(option, condition, true);
    }

    public ConditionOption(Option<T> option) {
        this(option, () -> true);
    }
}
