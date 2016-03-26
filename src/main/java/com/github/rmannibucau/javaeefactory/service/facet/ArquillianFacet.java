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
import java.util.Map;
import java.util.stream.Stream;

@ApplicationScoped
public class ArquillianFacet implements FacetGenerator, Versions {
    @Inject
    private TemplateRenderer tpl;

    @Inject
    private JAXRSFacet jaxrs;

    @Inject
    private OpenJPAFacet openjpa;

    private Dependency tomee;
    private Dependency arquillianTomee;
    private final Dependency arquillian = new Dependency("org.jboss.arquillian.junit", "arquillian-junit-container", ARQUILLIAN, "test");
    private final Dependency cxfClient = new Dependency("org.apache.cxf", "cxf-rt-rs-client", CXF, "test");
    private final Dependency johnzon = new Dependency("org.apache.johnzon", "johnzon-jaxrs", JOHNZON, "test");

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
        tomee = new Dependency("org.apache.tomee", "apache-tomee", TOMEE, "test", "zip", "webprofile");
        arquillianTomee = new Dependency("org.apache.tomee", "arquillian-tomee-remote", TOMEE, "test");
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(Dependency.junit(), tomee, arquillianTomee, arquillian, cxfClient, johnzon);
    }

    @Override
    public Stream<InMemoryFile> create(String packageBase, Build build, Collection<String> facets) {
        final String testBase = build.getTestJavaDirectory() + '/' + packageBase.replace('.', '/');
        final Map<String, String> model = new HashMap<String, String>() {{
            put("package", packageBase);
        }};

        final Collection<InMemoryFile> files = new ArrayList<>();
        if (facets.contains(jaxrs.name())) {
            files.add(new InMemoryFile(
                    testBase + "/jaxrs/ArquillianHelloResourceTest.java",
                    tpl.render("factory/jaxrs/ArquillianHelloResourceTest.java", model)));
        }
        if (facets.contains(openjpa.name())) {
            files.add(new InMemoryFile(
                    testBase + "/jpa/ArquillianHelloEntityTest.java",
                    tpl.render("factory/openjpa/ArquillianHelloEntityTest.java", model)));
        }
        files.add(new InMemoryFile(
                build.getTestResourcesDirectory() + "/arquillian.xml",
                tpl.render("factory/arquillian/arquillian.xml", new HashMap<String, String>() {{
                    put("buildDir", build.getBuildDir());
                }})));
        return files.stream();
    }

    @Override
    public String name() {
        return "Arquillian (test)";
    }

    @Override
    public String description() {
        return "Generates Arquillian test(s) with TomEE Remote.";
    }
}
