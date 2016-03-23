package com.github.rmannibucau.javaeefactory.service.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class ProjectRequest {
    private final String buildType;
    private final BuildConfiguration buildConfiguration;
    private final String packageBase;
    private final Collection<String> facets;

    @Getter
    @RequiredArgsConstructor
    public static class BuildConfiguration {
        private final String name;
        private final String description;
        private final String packaging;
        private final String group;
        private final String artifact;
        private final String version;
        private final String javaVersion;
    }
}
