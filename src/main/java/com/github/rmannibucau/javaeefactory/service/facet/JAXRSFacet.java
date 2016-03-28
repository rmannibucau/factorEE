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
import java.util.Map;
import java.util.stream.Stream;

@ApplicationScoped
public class JAXRSFacet implements FacetGenerator {
    @Inject
    private TemplateRenderer tpl;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        final Map<String, String> model = new HashMap<String, String>() {{
            put("package", packageBase);
        }};

        final String base = build.getMainJavaDirectory() + '/' + packageBase.replace('.', '/');
        return Stream.of(
                new InMemoryFile(base + "/jaxrs/ApplicationConfig.java", tpl.render("factory/jaxrs/ApplicationConfig.java", model)),
                new InMemoryFile(base + "/jaxrs/HelloResource.java", tpl.render("factory/jaxrs/HelloResource.java", model)),
                new InMemoryFile(base + "/jaxrs/Hello.java", tpl.render("factory/jaxrs/Hello.java", model))
        );
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(Dependency.javaeeApi());
    }

    @Override
    public String description() {
        return "Generate a Hello World JAX-RS endpoint.";
    }

    @Override
    public String name() {
        return "JAX-RS";
    }

    @Override
    public Category category() {
        return Category.CORE;
    }
}
