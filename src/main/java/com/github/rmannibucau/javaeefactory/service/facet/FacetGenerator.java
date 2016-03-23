package com.github.rmannibucau.javaeefactory.service.facet;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Stream;

public interface FacetGenerator {
    Stream<InMemoryFile> create(String packageBase, Build build, Collection<String> facets);
    Stream<Dependency> dependencies(Collection<String> facets);
    String description();

    @Getter
    @RequiredArgsConstructor
    class InMemoryFile {
        private final String path;
        private final String content;
    }
}
