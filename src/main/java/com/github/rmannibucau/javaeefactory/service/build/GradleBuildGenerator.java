package com.github.rmannibucau.javaeefactory.service.build;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.ApplicationComposerFacet;
import com.github.rmannibucau.javaeefactory.service.facet.OpenJPAFacet;
import com.github.rmannibucau.javaeefactory.service.facet.Versions;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;
import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.github.rmannibucau.javaeefactory.lang.Entries.entries;
import static com.github.rmannibucau.javaeefactory.lang.Entries.entry;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class GradleBuildGenerator implements BuildGenerator, Versions {
    @Inject
    private TemplateRenderer tpl;

    @Inject
    private OpenJPAFacet openjpa;

    @Inject
    private ApplicationComposerFacet applicationComposer;

    void register(@Observes final GeneratorRegistration init) {
        init.registerBuildType("Gradle", this);
    }

    @Override
    public Build createBuild(final ProjectRequest.BuildConfiguration buildConfiguration,
                             final String packageBase,
                             final Collection<Dependency> dependencies,
                             final Collection<String> facets) {
        final GradleBuild model = new GradleBuild(
                buildConfiguration,
                dependencies.stream().map(d -> "test".equals(d.getScope()) ? new Dependency(d, "testCompile") : d).collect(toList()),
                new LinkedHashSet<>(), new LinkedHashSet<>(),
                new LinkedHashSet<>("war".endsWith(buildConfiguration.getPackaging()) ? asList("java", "war") : singletonList("java")),
                new LinkedHashSet<>());
        return new Build(
                "src/main/java", "src/test/java",
                "src/main/resources", "src/test/resources",
                "src/main/webapp", "build.gradle",
                tpl.render("factory/gradle/build.gradle", enrichBuild(model, packageBase, facets)),
                "build");
    }

    private GradleBuild enrichBuild(final GradleBuild build, final String packageBase, final Collection<String> facets) {
        if (facets.contains(openjpa.name())) {
            build.getBuildDependencies().add("org.apache.openjpa:openjpa:" + OPENJPA); // override plugin one
            build.getBuildDependencies().add("at.schmutterer.oss.gradle:gradle-openjpa:0.2.0");
            build.getPlugins().add("openjpa");
            build.getPluginConfigurations().add(
                    entry("openjpa", entries("files =", "fileTree(sourceSets.main.output.classesDir).matching {\n    include '" + packageBase.replace('.', '/') + "/jpa/**'\n  }")));

            // embedded tests need the javaagent for openjpa to avoid surprises
            if (facets.contains(applicationComposer.name())) {
                // fake a scope to resolve the path of the javaagent programmatically
                build.getConfigurations().add("javaagentOpenJPA");
                build.getDependencies().add(new Dependency("org.apache.tomee", "openejb-javaagent", TOMEE, "javaagent"));

                // add the jaavagent computing the javaagent path
                build.getPluginConfigurations().add( // doFirst to delay it at exec time
                        entry("test.doFirst", entries("jvmArgs", "\"-javaagent:${configurations.javaagentOpenJPA.singleFile}\"")));
            }
        }
        return build;
    }

    @Data
    public static class GradleBuild {
        private final ProjectRequest.BuildConfiguration build;
        private final Collection<Dependency> dependencies;
        private final Collection<String> buildDependencies;
        private final Collection<String> configurations;
        private final Collection<String> plugins;
        private final Collection<Map.Entry<String, Collection<Map.Entry<String, String>>>> pluginConfigurations;
    }
}
