package com.github.rmannibucau.javaeefactory.jaxrs.model;

import lombok.Data;

import java.util.Collection;

@Data
public class ProjectModel {
    private String buildType;
    private String version;
    private String group;
    private String artifact;
    private String name;
    private String description;
    private String packageBase;
    private String packaging;
    private String javaVersion;
    private Collection<String> facets;
}
