package com.wishtoday.ts.simpleminer.core.matcher;

import org.jetbrains.annotations.NotNull;

public interface MatcherParser<T> {
    boolean shouldParse(@NotNull String value);
    T parse(String string);
}
