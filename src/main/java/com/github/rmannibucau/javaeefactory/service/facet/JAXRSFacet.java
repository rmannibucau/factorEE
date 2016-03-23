package com.github.rmannibucau.javaeefactory.service.facet;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@ApplicationScoped
public class JAXRSFacet implements FacetGenerator {
    @Inject
    private ApplicationComposerFacet applicationComposerFacet;

    @Inject
    private ArquillianFacet arquillianFacet;

    @Inject
    private TemplateRenderer tpl;

    private final Dependency openjbCxfRs = new Dependency("org.apache.tomee", "openejb-cxf-rs", "7.0.0-M3", "test");
    private final Dependency cxfClient = new Dependency("org.apache.cxf", "cxf-rt-rs-client", "3.1.5", "test");
    private final Dependency johnzon = new Dependency("org.apache.johnzon", "johnzon-jaxrs", "0.9.3-incubating", "test");

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType("JAX-RS", this);
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        final String packageSuffix = '/' + packageBase.replace('.', '/');
        final Map<String, String> model = new HashMap<String, String>() {{
            put("package", packageBase);
        }};

        final Collection<InMemoryFile> streams = new HashSet<>();
        streams.addAll(createMainFiles(build.getMainJavaDirectory() + packageSuffix, model));
        if (facets.contains(applicationComposerFacet.getName())) {
            streams.add(newApplicationComposerTest(build.getTestJavaDirectory() + packageSuffix, model));
        }
        if (facets.contains(arquillianFacet.getName())) {
            streams.add(newArquillianTest(build.getTestJavaDirectory() + packageSuffix, model));
            streams.add(arquillianFacet.createArquillianXml(build.getTestResourcesDirectory(), build.getBuildDir()));
        }
        return streams.stream();
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        final Collection<Dependency> deps = new ArrayList<>();
        deps.add(Dependency.javaeeApi());
        if (facets.contains(applicationComposerFacet.getName())) {
            deps.add(openjbCxfRs);
        }
        if (facets.contains(arquillianFacet.getName())) {
            deps.add(cxfClient);
            deps.add(johnzon);
        }
        return deps.stream();
    }

    @Override
    public String description() {
        return "Generate a Hello World JAX-RS endpoint.";
    }

    private InMemoryFile newArquillianTest(final String base, final Map<String, String> model) {
        return new InMemoryFile(
                base + "/jaxrs/ArquillianHelloResourceTest.java",
                tpl.render("factory/jaxrs/ArquillianHelloResourceTest.java", model));
    }

    private InMemoryFile newApplicationComposerTest(final String base, final Map<String, String> model) {
        return new InMemoryFile(
                base + "/jaxrs/ApplicationComposerHelloResourceTest.java",
                tpl.render("factory/jaxrs/ApplicationComposerHelloResourceTest.java", model));
    }

    private Collection<InMemoryFile> createMainFiles(final String base, final Map<String, String> model) {
        return asList(
                new InMemoryFile(base + "/jaxrs/ApplicationConfig.java", tpl.render("factory/jaxrs/ApplicationConfig.java", model)),
                new InMemoryFile(base + "/jaxrs/HelloResource.java", tpl.render("factory/jaxrs/HelloResource.java", model)),
                new InMemoryFile(base + "/jaxrs/Hello.java", tpl.render("factory/jaxrs/Hello.java", model))
        );
    }
}
