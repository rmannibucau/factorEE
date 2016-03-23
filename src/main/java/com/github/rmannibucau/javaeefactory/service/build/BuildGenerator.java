package com.github.rmannibucau.javaeefactory.service.build;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;

import java.util.Collection;

public interface BuildGenerator {
    Build createBuild(ProjectRequest.BuildConfiguration buildConfiguration, Collection<Dependency> dependencies);
}
