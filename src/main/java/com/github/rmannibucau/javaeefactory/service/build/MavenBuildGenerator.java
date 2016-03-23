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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@ApplicationScoped
public class MavenBuildGenerator implements BuildGenerator {
    @Inject
    private TemplateRenderer renderer;

    private Map<String, Collection<Plugin>> plugins;

    void register(@Observes final GeneratorRegistration init) {
        init.registerBuildType("Maven", this);
        plugins = new HashMap<>();
        plugins.put("jar", emptyList());
        plugins.put("war", asList(
                new Plugin("org.apache.tomee.maven", "tomee-embedded-maven-plugin", "7.0.0-M3", new LinkedHashMap<String,String>() {{
                    put("context", "/${project.artifactId}");
                    put("classpathAsWar", "true");
                    put("webResourceCached", "false");
                }}.entrySet()),
                new Plugin("org.apache.tomee.maven", "tomee-maven-plugin", "7.0.0-M3", new LinkedHashMap<String, String>() {{
                    put("context", "/${project.artifactId}");
                }}.entrySet()),
                new Plugin("org.apache.maven.plugins", "maven-war-plugin", "2.6", new LinkedHashMap<String, String>() {{
                    put("failOnMissingWebXml", "false");
                }}.entrySet())
        ));
    }

    @Override
    public Build createBuild(final ProjectRequest.BuildConfiguration buildConfiguration,
                             final Collection<Dependency> dependencies) {
        return new Build(
                "src/main/java", "src/test/java",
                "src/main/resources", "src/test/resources",
                "src/main/webapp", "pom.xml",
                renderer.render("factory/maven/pom.xml", new Pom(buildConfiguration, dependencies, plugins.get(buildConfiguration.getPackaging()))),
                "target");
    }

    @Data
    public static class Pom {
        private final ProjectRequest.BuildConfiguration build;
        private final Collection<Dependency> dependencies;
        private final Collection<Plugin> plugins;
    }

    @Data
    public static class Plugin {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final Collection<Map.Entry<String, String>> configuration;
    }
}
