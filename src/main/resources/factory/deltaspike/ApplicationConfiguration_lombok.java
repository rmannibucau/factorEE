package {{package}}.deltaspike;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.Getter;
import org.apache.deltaspike.core.api.config.ConfigProperty;

@Getter
@ApplicationScoped
public class ApplicationConfiguration {
    @Inject
    @ConfigProperty(name = "{{prefix}}.api.version", defaultValue  = "1.0")
    private String apiVersion;
}