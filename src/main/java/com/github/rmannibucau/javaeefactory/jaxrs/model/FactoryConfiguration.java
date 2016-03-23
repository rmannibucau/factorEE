package com.github.rmannibucau.javaeefactory.jaxrs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryConfiguration {
    private Collection<String> buildTypes;
    private Map<String, String> facets;
}
