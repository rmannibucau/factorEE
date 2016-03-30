package com.github.rmannibucau.javaeefactory.service.facet.libraries;

import com.github.rmannibucau.javaeefactory.service.domain.Dependency;
import com.github.rmannibucau.javaeefactory.service.event.GeneratorRegistration;
import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Collection;
import java.util.stream.Stream;

@ApplicationScoped
public class LombokFacet implements FacetGenerator {
    private Dependency dependency;

    void register(@Observes final GeneratorRegistration init) {
        init.registerFacetType(this);
        dependency = new Dependency("org.projectlombok", "lombok", "1.16.8", "provided");
    }

    @Override
    public String description() {
        return "Synthaxic sugar for Java.";
    }

    @Override
    public String name() {
        return "Lombok";
    }

    @Override
    public Category category() {
        return Category.LIBRARIES;
    }

    @Override
    public Stream<Dependency> dependencies(final Collection<String> facets) {
        return Stream.of(dependency);
    }

    @Override
    public String readme() {
        return "Lombok provides an annotation processor allowing to generate at compile time \n" +
                "most boring java patterns like getter/setter/equals/hashcode/toString/... (@Data for instance).\n" +
                "More on https://projectlombok.org/.";
    }
}
