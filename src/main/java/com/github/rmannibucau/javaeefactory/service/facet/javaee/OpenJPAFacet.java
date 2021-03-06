package com.github.rmannibucau.javaeefactory.service.facet.javaee;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;
import com.github.rmannibucau.javaeefactory.service.facet.libraries.LombokFacet;
import com.github.rmannibucau.javaeefactory.service.facet.Versions;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@ApplicationScoped
public class OpenJPAFacet implements FacetGenerator,Versions {
    @Inject
    private TemplateRenderer tpl;

    @Inject
    private LombokFacet lombokFacet;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        final Map<String, String> model = new HashMap<String, String>() {{
            put("package", packageBase);
        }};
        return Stream.of(
                new InMemoryFile(
                        build.getMainJavaDirectory() + '/' + packageBase.replace('.', '/') + "/jpa/HelloEntity.java",
                        facets.contains(lombokFacet.name()) ?
                                tpl.render("factory/openjpa/HelloEntity_lombok.java", model) : tpl.render("factory/openjpa/HelloEntity.java", model)
                ),
                new InMemoryFile(
                        build.getMainResourcesDirectory() + "/META-INF/persistence.xml",
                        tpl.render("factory/openjpa/persistence.xml", emptyMap()))
        );
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(
                Dependency.javaeeApi(),
                // adding openjpa to avoid compilation error on enhanced classes
                new Dependency("org.apache.openjpa", "openjpa", OPENJPA, "provided")
        );
    }

    @Override
    public String description() {
        return "OpenJPA as JPA provider";
    }

    @Override
    public String name() {
        return "OpenJPA";
    }

    @Override
    public String readme() {
        return "OpenJPA is the Apache JPA provider. For now it targets JPA version 2.0.";
    }

    @Override
    public Category category() {
        return Category.CORE;
    }
}
