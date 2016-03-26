package com.github.rmannibucau.javaeefactory.jaxrs;

import com.github.rmannibucau.javaeefactory.jaxrs.model.FactoryConfiguration;
import com.github.rmannibucau.javaeefactory.jaxrs.model.ProjectModel;
import com.github.rmannibucau.javaeefactory.service.ProjectGenerator;
import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Path("factory")
@ApplicationScoped
public class ProjectFactoryResource {
    @Inject
    private ProjectGenerator generator;

    private FactoryConfiguration configuration;

    @PostConstruct
    private void init() {
        configuration = new FactoryConfiguration(
                new ArrayList<>(generator.getGenerators().keySet()),
                new HashMap<>(generator.getFacets().entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().description()))));
    }

    @POST
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    public StreamingOutput create(final ProjectModel model) {
        return out -> generator.generate(toRequest(model), out);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FactoryConfiguration getConfiguration() {
        return configuration;
    }

    private ProjectRequest toRequest(final ProjectModel model) {
        String packageBase = ofNullable(model.getGroup()).orElse("com.application").replace('/', '.');
        if (packageBase.endsWith(".")) {
            packageBase = packageBase.substring(0, packageBase.length() - 1);
        }
        if (packageBase.isEmpty()) {
            packageBase = "application";
        }

        return new ProjectRequest(
                ofNullable(model.getBuildType()).orElse("maven").toLowerCase(Locale.ENGLISH),
                new ProjectRequest.BuildConfiguration(
                        ofNullable(model.getName()).orElse("A Factory generated Application"),
                        ofNullable(model.getDescription()).orElse("An application generated by the Factory"),
                        ofNullable(model.getPackaging()).orElse("war"),
                        packageBase,
                        ofNullable(model.getArtifact()).orElse("application"),
                        ofNullable(model.getVersion()).orElse("0.0.1-SNAPSHOT"),
                        ofNullable(model.getJavaVersion()).orElse("1.8")),
                ofNullable(model.getPackageBase()).orElse("com.application").replace('/', '.'),
                ofNullable(model.getFacets()).orElse(emptyList()));
    }
}
