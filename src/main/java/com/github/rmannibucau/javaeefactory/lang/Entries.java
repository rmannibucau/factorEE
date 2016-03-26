package com.github.rmannibucau.javaeefactory.lang;

import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Entries {
    public static <T> Map.Entry<String, T> entry(final String key, final T value) {
        return entries(key, value).iterator().next();
    }

    public static <T> Collection<Map.Entry<String, T>> entries(final String key, final T value) {
        return new HashMap<String, T>() {{
            put(key, value);
        }}.entrySet();
    }
}
