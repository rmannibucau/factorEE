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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static com.github.rmannibucau.javaeefactory.lang.MapCollectors.mergeCollections;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
                generator.getFacets().values().stream()
                        .collect(toMap(
                                e -> e.category().getHumanName(), e -> new ArrayList<>(singletonList(new FactoryConfiguration.Facet(e.name(), e.description()))),
                                mergeCollections(),
                                TreeMap::new)));
        // sort them all
        Collections.sort(configuration.getBuildTypes());
        configuration.getFacets().forEach((k, v) -> Collections.sort(v, (o1, o2) -> o1.getName().compareTo(o2.getName())));
    }

    @POST // as a webservice it is easier to use
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    public StreamingOutput create(final ProjectModel model) {
        return out -> generator.generate(toRequest(model), out);
    }

    @GET
    @Path("zip")
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getZip(
            @QueryParam("buildType") final String buildType,
            @QueryParam("version") final String version,
            @QueryParam("group") final String group,
            @QueryParam("artifact") final String artifact,
            @QueryParam("name") final String name,
            @QueryParam("description") final String description,
            @QueryParam("packageBase") final String packageBase,
            @QueryParam("packaging") final String packaging,
            @QueryParam("javaVersion") final String javaVersion,
            @QueryParam("facets") final List<String> facets) {
        final ProjectModel model = new ProjectModel();
        model.setBuildType(buildType);
        model.setVersion(version);
        model.setGroup(group);
        model.setArtifact(artifact);
        model.setName(name);
        model.setDescription(description);
        model.setPackageBase(packageBase);
        model.setPackaging(packaging);
        model.setJavaVersion(javaVersion);
        model.setFacets(ofNullable(facets).orElse(emptyList()));
        return Response.ok((StreamingOutput) output -> generator.generate(toRequest(model), output))
                .header("Content-Disposition", ofNullable(artifact).orElse("example") + '-' + ofNullable(version).orElse("0") + ".zip")
                .build();
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
