package com.github.rmannibucau.javaeefactory.service.build;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.Versions;
import com.github.rmannibucau.javaeefactory.service.facet.javaee.OpenJPAFacet;
import com.github.rmannibucau.javaeefactory.service.facet.testing.ApplicationComposerFacet;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;
import lombok.Data;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.rmannibucau.javaeefactory.lang.Entries.entries;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

@ApplicationScoped
public class MavenBuildGenerator implements BuildGenerator, Versions {
    @Inject
    private TemplateRenderer renderer;

    @Inject
    private OpenJPAFacet openjpa;

    @Inject
    private ApplicationComposerFacet applicationComposer;

    private Map<String, Collection<Plugin>> plugins;

    void register(@Observes final GeneratorRegistration init) {
        init.registerBuildType("Maven", this);
        plugins = new HashMap<>();
        plugins.put("jar", emptyList());
        plugins.put("war", asList(
                new Plugin(
                        "org.apache.tomee.maven", "tomee-embedded-maven-plugin", "7.0.0-M3",
                        emptySet(),
                        new LinkedHashMap<String, String>() {{
                            put("context", "/${project.artifactId}");
                            put("classpathAsWar", "true");
                            put("webResourceCached", "false");
                        }}.entrySet()),
                new Plugin(
                        "org.apache.tomee.maven", "tomee-maven-plugin", "7.0.0-M3",
                        emptySet(), new LinkedHashMap<String, String>() {{
                    put("context", "/${project.artifactId}");
                }}.entrySet()),
                new Plugin(
                        "org.apache.maven.plugins", "maven-war-plugin", "2.6",
                        emptySet(),
                        new LinkedHashMap<String, String>() {{
                            put("failOnMissingWebXml", "false");
                        }}.entrySet())
        ));
    }

    @Override
    public Build createBuild(final ProjectRequest.BuildConfiguration buildConfiguration,
                             final String packageBase,
                             final Collection<Dependency> dependencies,
                             final Collection<String> facets) {
        return new Build(
                "src/main/java", "src/test/java",
                "src/main/resources", "src/test/resources",
                "src/main/webapp", "pom.xml",
                renderer.render(
                        "factory/maven/pom.xml", new Pom(buildConfiguration, dependencies,
                                createPlugins(plugins.get(buildConfiguration.getPackaging()), packageBase, facets))),
                "target");
    }

    private Collection<Plugin> createPlugins(final Collection<Plugin> plugins,
                                             final String packageBase,
                                             final Collection<String> facets) {
        final Collection<Plugin> buildPlugins = new ArrayList<>(plugins);

        if (facets.contains(openjpa.name())) {
            buildPlugins.add(
                    new Plugin(
                            "org.apache.openjpa", "openjpa-maven-plugin", OPENJPA,
                            singletonList(new Execution("openjpa-enhance", "process-classes", "enhance")),
                            entries("includes", packageBase.replace('.', '/') + "/jpa/*.class")));
        }

        buildPlugins.add(new Plugin(
                "org.apache.maven.plugins", "maven-surefire-plugin", SUREFIRE, emptySet(), new LinkedHashMap<String, String>() {{
            put("trimStackTrace", "false");
            put("runOrder", "alphabetical");
            if (facets.contains(openjpa.name()) && facets.contains(applicationComposer.name())) {
                put("argLine", "\"-javaagent:${settings.localRepository}/org/apache/tomee/openejb-javaagent/" + TOMEE + "/openejb-javaagent-" + TOMEE + ".jar\"");
            }
        }}.entrySet()));

        return buildPlugins;
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
        private final Collection<Execution> executions;
        private final Collection<Map.Entry<String, String>> configuration;
    }

    @Data
    public static class Execution {
        private final String id;
        private final String phase;
        private final String goal;
    }
}
