package com.github.rmannibucau.javaeefactory.service.facet.testing;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;
import com.github.rmannibucau.javaeefactory.service.facet.Versions;
import com.github.rmannibucau.javaeefactory.service.facet.javaee.JAXRSFacet;
import com.github.rmannibucau.javaeefactory.service.facet.javaee.OpenJPAFacet;
import com.github.rmannibucau.javaeefactory.service.facet.libraries.deltaspike.DeltaspikeConfiguration;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@ApplicationScoped
public class ApplicationComposerFacet implements FacetGenerator, Versions {
    @Inject
    private JAXRSFacet jaxrs;

    @Inject
    private OpenJPAFacet openjpa;

    @Inject
    private DeltaspikeConfiguration deltaspikeConfiguration;

    @Inject
    private TemplateRenderer tpl;

    private Dependency openejbCore;
    private Dependency openejbCxfRs;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
        openejbCore = new Dependency("org.apache.tomee", "openejb-core", TOMEE, "test");
        openejbCxfRs = new Dependency("org.apache.tomee", "openejb-cxf-rs", TOMEE, "test");
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        final Map<String, String> model = new HashMap<String, String>() {{
            put("package", packageBase);
        }};

        final Collection<InMemoryFile> files = new ArrayList<>();
        if (facets.contains(jaxrs.name())) {
            files.add(new InMemoryFile(
                    build.getTestJavaDirectory() + '/' + packageBase.replace('.', '/') + "/jaxrs/ApplicationComposerHelloResourceTest.java",
                    tpl.render("factory/jaxrs/ApplicationComposerHelloResourceTest.java", model)));
        }
        if (facets.contains(openjpa.name())) {
            files.add(new InMemoryFile(
                    build.getTestJavaDirectory() + '/' + packageBase.replace('.', '/') + "/jpa/ApplicationComposerHelloEntityTest.java",
                    tpl.render("factory/openjpa/ApplicationComposerHelloEntityTest.java", model)));
        }
        if (facets.contains(deltaspikeConfiguration.name())) {
            files.add(new InMemoryFile(
                    build.getTestJavaDirectory() + '/' + packageBase.replace('.', '/') + "/deltaspike/ApplicationComposerConfigurationTest.java",
                    tpl.render("factory/deltaspike/ApplicationComposerConfigurationTest.java", model)));
        }
        return files.stream();
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        if (facets.contains(jaxrs.name())) {
            return Stream.of(Dependency.junit(), openejbCxfRs);
        }
        return Stream.of(Dependency.junit(), openejbCore);
    }

    @Override
    public String name() {
        return "ApplicationComposer";
    }

    @Override
    public Category category() {
        return Category.TEST;
    }

    @Override
    public String description() {
        return "Generates ApplicationComposer based test(s).";
    }

    @Override
    public String readme() {
        return "ApplicationComposer is a TomEE feature allowing to:\n" +
                "\n" +
                "- test with JUnit or TestNG any EE application in embedded mode building programmatically the EE model instead of relying on scanning\n" +
                "- deploy a standalone application using ApplicationComposers helper (very useful for application without frontend like batch)";
    }
}
