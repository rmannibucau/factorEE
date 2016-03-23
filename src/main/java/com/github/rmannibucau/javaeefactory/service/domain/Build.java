package com.github.rmannibucau.javaeefactory.service.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Build {
    private final String mainJavaDirectory;
    private final String testJavaDirectory;
    private final String mainResourcesDirectory;
    private final String testResourcesDirectory;
    private final String mainWebResourcesDirectory;
    private final String buildFileName;
    private final String buildFileContent;
    private final String buildDir;
}
