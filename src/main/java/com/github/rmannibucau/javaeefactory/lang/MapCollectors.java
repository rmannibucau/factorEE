package com.github.rmannibucau.javaeefactory.lang;

import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.function.BinaryOperator;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class MapCollectors {
    public static <C extends Collection<T>, T> BinaryOperator<C> mergeCollections() {
        return (u, u2) -> {
            if (u == null) {
                return u2;
            }
            u.addAll(u2);
            return u;
        };
    }
}
