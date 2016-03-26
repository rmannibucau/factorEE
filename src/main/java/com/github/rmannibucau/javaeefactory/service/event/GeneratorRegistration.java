package com.github.rmannibucau.javaeefactory.service.event;

import com.github.rmannibucau.javaeefactory.service.build.BuildGenerator;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GeneratorRegistration {
    private final Map<String, BuildGenerator> buildGenerators = new HashMap<>();
    private final Map<String, FacetGenerator> facetGenerators = new HashMap<>();

    public GeneratorRegistration registerBuildType(final String build, final BuildGenerator generator) {
        buildGenerators.put(build, generator);
        return this;
    }

    public GeneratorRegistration registerFacetType(final FacetGenerator generator) {
        facetGenerators.put(generator.name(), generator);
        return this;
    }
}
