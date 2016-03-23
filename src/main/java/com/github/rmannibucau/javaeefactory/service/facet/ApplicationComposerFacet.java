package com.github.rmannibucau.javaeefactory.service.facet;

import com.github.rmannibucau.javaeefactory.service.domain.Build;
import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Collection;
import java.util.stream.Stream;

@ApplicationScoped
public class ApplicationComposerFacet implements FacetGenerator {
    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(getName(), this);
    }

    public String getName() {
        return "ApplicationComposer (test)";
    }

    @Override
    public Stream<InMemoryFile> create(final String packageBase, final Build build, final Collection<String> facets) {
        return Stream.empty(); // placeholder used in other facets
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        // note: other facets add the dependencies depending what they need, that's why we don't add openejb-core
        return Stream.of(Dependency.junit());
    }

    @Override
    public String description() {
        return "Generates ApplicationComposer based test(s).";
    }
}
