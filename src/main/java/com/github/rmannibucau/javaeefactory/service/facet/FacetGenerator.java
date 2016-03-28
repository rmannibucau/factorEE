package com.github.rmannibucau.javaeefactory.service.facet;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Stream;

public interface FacetGenerator {
    String description();

    String name();

    Category category();

    default Stream<InMemoryFile> create(final String packageBase, final Build build,
                                        final Collection<String> facets) {
        return Stream.empty();
    }

    default Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.empty();
    }

    @Getter
    @RequiredArgsConstructor
    class InMemoryFile {
        private final String path;
        private final String content;
    }

    @RequiredArgsConstructor
    enum Category {
        CORE("Core"), TEST("Test");

        @Getter
        private final String humanName;
    }
}
