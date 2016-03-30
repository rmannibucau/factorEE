package {{package}}.deltaspike;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.ConfigProperty;

@ApplicationScoped
public class ApplicationConfiguration {
    @Inject
    @ConfigProperty(name = "{{prefix}}.api.version", defaultValue  = "1.0")
    private String apiVersion;

    public String getApiVersion() {
        return apiVersion;
    }
}