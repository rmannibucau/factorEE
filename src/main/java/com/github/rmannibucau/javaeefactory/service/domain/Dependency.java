package com.github.rmannibucau.javaeefactory.service.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class Dependency {
    private static final Dependency JAVAEE_API = new Dependency("org.apache.tomee", "javaee-api", "7.0", "provided");
    private static final Dependency JUNIT = new Dependency("junit", "junit", "4.12", "test");

    private final String group;
    private final String artifact;
    private final String version;
    private final String scope;
    private final String type;
    private final String classifier;

    public Dependency(final String group, final String artifact, final String version, final String scope) {
        this(group, artifact, version, scope, null, null);
    }

    public static Dependency javaeeApi() {
        return JAVAEE_API;
    }

    public static Dependency junit() {
        return JUNIT;
    }
}
