package com.github.rmannibucau.javaeefactory.service;

import com.github.rmannibucau.javaeefactory.service.facet.FacetGenerator;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.github.rmannibucau.javaeefactory.lang.MapCollectors.mergeCollections;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class ReadmeGenerator {
    public String createReadme(final String name, final Map<FacetGenerator, List<String>> filesPerFacet) {
        final Map<String, List<FacetGenerator>> facetByCategory = filesPerFacet.keySet().stream()
                .collect(toMap(f -> f.category().getHumanName(), f -> new ArrayList<>(singletonList(f)), mergeCollections(), TreeMap::new));


        final StringBuilder builder = new StringBuilder();
        builder.append("= ").append(name).append("\n\n");

        facetByCategory.forEach((c, f) -> {
            builder.append("== ").append(c).append("\n\n");
            f.stream()
                    .sorted((o1, o2) -> o1.name().compareTo(o2.name()))
                    .forEach(facet -> {
                        builder.append("=== ").append(facet.name()).append("\n\n");
                        builder.append(ofNullable(facet.readme()).orElseGet(() -> ofNullable(facet.description()).orElse("")));
                        builder.append("\n\n");

                        final Collection<String> files = filesPerFacet.get(facet);
                        if (files != null && !files.isEmpty()) {
                            builder.append("==== Files generated by this facet\n\n");
                            files.forEach(file -> builder.append("- ").append(file).append("\n"));
                        }

                        builder.append("\n\n");
                    });
        });

        return builder.toString();
    }
}
