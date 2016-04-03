package com.github.rmannibucau.javaeefactory.service;

import com.github.rmannibucau.javaeefactory.service.build.BuildGenerator;
import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;
import com.github.rmannibucau.javaeefactory.service.event.CreateProject;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@ApplicationScoped
public class ProjectGenerator {
    @Getter
    private final Map<String, BuildGenerator> generators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Getter
    private final Map<String, FacetGenerator> facets = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Inject
    private Event<GeneratorRegistration> registrationEvent;

    @Inject
    private ReadmeGenerator readmeGenerator;

    @Inject
    private Event<CreateProject> onCreate;

    private List<String> scopesOrdering;

    @PostConstruct
    private void init() {
        final GeneratorRegistration event = new GeneratorRegistration();
        registrationEvent.fire(event);
        generators.putAll(event.getBuildGenerators());
        facets.putAll(event.getFacetGenerators());
        scopesOrdering = asList("provided", "compile", "runtime", "test");
    }

    public void generate(final ProjectRequest request, final OutputStream outputStream) {
        final BuildGenerator generator = generators.get(request.getBuildType());
        final Map<String, String> files = new HashMap<>();

        // build dependencies to give them to the build
        final Collection<String> facets = ofNullable(request.getFacets()).orElse(emptyList());
        final List<Dependency> dependencies = new ArrayList<>(facets.stream()
                .map(this.facets::get)
                .flatMap(f -> f.dependencies(facets))
                .collect(toSet()));
        Collections.sort(dependencies, (o1, o2) -> {
            {// by scope
                final int scope1 = scopesOrdering.indexOf(o1.getScope());
                final int scope2 = scopesOrdering.indexOf(o2.getScope());
                final int scopeDiff = scope1 - scope2;
                if (scopeDiff != 0) {
                    return scopeDiff;
                }
            }

            {// by group
                final int comp = o1.getGroup().compareTo(o2.getGroup());
                if (comp != 0) {
                    return comp;
                }
            }

            // by name
            return o1.getArtifact().compareTo(o2.getArtifact());
        });
        // force javaee-api and force it first
        dependencies.remove(Dependency.javaeeApi());
        dependencies.add(0, Dependency.javaeeApi());

        // create the build to be able to generate the files
        final Build build = generator.createBuild(request.getBuildConfiguration(), request.getPackageBase(), dependencies, facets);
        files.put(build.getBuildFileName(), build.getBuildFileContent());

        // activate CDI
        files.put("war".equals(request.getBuildConfiguration().getPackaging()) ?
                        build.getMainWebResourcesDirectory() + "/WEB-INF/beans.xml" :
                        build.getMainResourcesDirectory() + "/META-INF/beans.xml",
                "<?xml version=\"1.0\"?>\n<beans xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" bean-discovery-mode=\"all\" version=\"1.1\" />\n");

        // generate facet files
        final Map<FacetGenerator, List<String>> filePerFacet = facets.stream()
                .map(s -> s.toLowerCase(Locale.ENGLISH))
                .collect(toMap(this.facets::get, f -> {
                    final FacetGenerator g = this.facets.get(f);
                    return g.create(request.getPackageBase(), build, facets)
                            .map(file -> {
                                files.put(file.getPath(), file.getContent());
                                return file;
                            })
                            .map(FacetGenerator.InMemoryFile::getPath)
                            .collect(toList());
                }));

        // generate README.adoc if needed
        if (!files.containsKey("README.adoc")) {
            files.put("README.adoc", readmeGenerator.createReadme(request.getBuildConfiguration().getName(), filePerFacet));
        }

        // now create the zip prefixing it with the artifact value
        final String rootName = request.getBuildConfiguration().getArtifact();
        final Set<String> createdFolders = new HashSet<>();
        try (final ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            // first create folders
            new HashSet<>(files.keySet()).forEach(path -> {
                final String[] segments = (rootName + '/' + path).split("/");
                final StringBuilder current = new StringBuilder();
                for (int i = 0; i < segments.length; i++) {
                    if (i == segments.length - 1) {
                        break;
                    }

                    current.append(segments[i]).append('/');

                    final String folder = current.toString();
                    if (createdFolders.add(folder)) {
                        try {
                            zip.putNextEntry(new ZipEntry(folder));
                            zip.closeEntry();
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            });

            // now create files entries
            files.forEach((path, content) -> {
                try {
                    zip.putNextEntry(new ZipEntry(rootName + '/' + path));
                    zip.write(content.replace("\r", "")/*avoid side effect of the OS*/.getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        onCreate.fire(new CreateProject(facets));
    }
}
