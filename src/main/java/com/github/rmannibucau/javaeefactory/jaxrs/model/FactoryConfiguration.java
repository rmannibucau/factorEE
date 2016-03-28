package com.github.rmannibucau.javaeefactory.jaxrs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryConfiguration {
    private List<String> buildTypes;
    private Map<String, List<Facet>> facets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Facet {
        private String name;
        private String description;
    }
}
