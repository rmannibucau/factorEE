package com.github.rmannibucau.javaeefactory.service.build;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;
import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class GradleBuildGenerator implements BuildGenerator {
    @Inject
    private TemplateRenderer tpl;

    void register(@Observes final GeneratorRegistration init) {
        init.registerBuildType("Gradle", this);
    }

    @Override
    public Build createBuild(final ProjectRequest.BuildConfiguration buildConfiguration, final Collection<Dependency> dependencies) {
        return new Build(
                "src/main/java", "src/test/java",
                "src/main/resources", "src/test/resources",
                "src/main/webapp", "build.gradle",
                tpl.render("factory/gradle/build.gradle", new GradleBuild("war".endsWith(buildConfiguration.getPackaging()) ? "war" : "java", buildConfiguration, dependencies)),
                "build");
    }

    @Data
    public static class GradleBuild {
        private final String pluginType;
        private final ProjectRequest.BuildConfiguration build;
        private final Collection<Dependency> dependencies;
    }
}
