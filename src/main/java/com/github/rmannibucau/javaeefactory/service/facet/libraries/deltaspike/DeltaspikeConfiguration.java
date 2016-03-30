package com.github.rmannibucau.javaeefactory.service.facet.libraries.deltaspike;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;
import com.github.rmannibucau.javaeefactory.service.facet.Versions;
import com.github.rmannibucau.javaeefactory.service.facet.libraries.LombokFacet;
import com.github.rmannibucau.javaeefactory.service.template.TemplateRenderer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class DeltaspikeConfiguration implements FacetGenerator, Versions {
    @Inject
    private TemplateRenderer tpl;

    @Inject
    private LombokFacet lombokFacet;

    private Dependency deltaspikeCoreApi;
    private Dependency deltaspikeCoreImpl;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
        deltaspikeCoreApi = new Dependency("org.apache.deltaspike.core", "deltaspike-core-api", DELTASPIKE, "compile");
        deltaspikeCoreImpl = new Dependency("org.apache.deltaspike.core", "deltaspike-core-impl", DELTASPIKE, "runtime");
    }

    @Override
    public String description() {
        return "@Inject your @ConfigProperty values.";
    }

    @Override
    public String name() {
        return "Deltaspike Configuration";
    }

    @Override
    public Category category() {
        return Category.LIBRARIES;
    }

    @Override
    public String readme() {
        return "DeltaSpike provides a set of JavaEE CDI extensions.\n" +
                "Configuration one allows to use a smooth CDI API to get its configuration \n" +
                "injected keeping an extensible and advanced value reading system matching all \n" +
                "enterprise configuration choices.";
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(deltaspikeCoreApi, deltaspikeCoreImpl);
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        final Map<String, String> model = new HashMap<String, String>() {{
            put("prefix", ofNullable(packageBase).map(p -> {
                final int lastDot = p.lastIndexOf('.');
                return lastDot > 0 && lastDot != packageBase.length() - 1 ? p.substring(lastDot + 1) : p;
            }).orElse("application"));
            put("package", packageBase);
        }};

        final String base = build.getMainJavaDirectory() + '/' + packageBase.replace('.', '/');
        return Stream.of(new InMemoryFile(
                base + "/deltaspike/ApplicationConfiguration.java",
                tpl.render("factory/deltaspike/ApplicationConfiguration" + (facets.contains(lombokFacet.name()) ? "_lombok" : "") + ".java", model)));
    }
}
