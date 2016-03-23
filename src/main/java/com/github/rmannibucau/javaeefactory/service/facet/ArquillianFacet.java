package com.github.rmannibucau.javaeefactory.service.facet;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Stream;

@ApplicationScoped
public class ArquillianFacet implements FacetGenerator {
    @Inject
    private TemplateRenderer tpl;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(getName(), this);
    }

    public String getName() {
        return "Arquillian (test)";
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        return Stream.empty(); // placeholder used in other facets
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(
                Dependency.junit(),
                new Dependency("org.apache.tomee", "apache-tomee", "7.0.0-M3", "test", "zip", "webprofile"), // to be cached locally
                new Dependency("org.apache.tomee", "arquillian-tomee-remote", "7.0.0-M3", "test"),
                new Dependency("org.jboss.arquillian.junit", "arquillian-junit-container", "1.1.11.Final", "test")
        );
    }

    @Override
    public String description() {
        return "Generates Arquillian test(s) with TomEE Remote.";
    }

    public InMemoryFile createArquillianXml(final String testResourceDir, final String buildDir) {
        return new InMemoryFile(
                testResourceDir + "/arquillian.xml",
                tpl.render("factory/arquillian/arquillian.xml", new HashMap<String, String>() {{
                    put("buildDir", buildDir);
                }}));
    }
}
