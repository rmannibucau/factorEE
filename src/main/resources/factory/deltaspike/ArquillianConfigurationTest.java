package {{package}}.deltaspike;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.impl.config.DefaultConfigPropertyProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ArquillianConfigurationTest {
    @Deployment
    public static Archive<?> createDeploymentPackage() {
        return ShrinkWrap.create(WebArchive.class, "configuration.war")
                .addClasses(ApplicationConfiguration.class)
                .addAsLibraries(
                        // add deltaspike library, using ziplock which locates in current classpath jar from a class
                        jarLocation(ConfigProperty.class),
                        jarLocation(DefaultConfigPropertyProducer.class)
                )
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private ApplicationConfiguration configuration;

    @Test
    public void checkApiVersion() {
        assertEquals("1.0", configuration.getApiVersion());
    }
}

