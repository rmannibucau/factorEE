package com.github.rmannibucau.javaeefactory.service.event;

import lombok.Data;

import java.util.Collection;

@Data
public class CreateProject {
    private final Collection<String> facets;
}
