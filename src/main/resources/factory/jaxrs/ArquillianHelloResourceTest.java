package {{package}}.jaxrs;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ArquillianHelloResourceTest {
    @Deployment(testable = false) // testable = false implies it is a client test so no server injection
    public static Archive<?> createDeploymentPackage() {
        return ShrinkWrap.create(WebArchive.class, "app.war")
                .addPackage(Hello.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    private URL url;

    @Test
    public void sayHelloToTheDefaultUser() {
        assertEquals(
                "test",
                ClientBuilder.newBuilder().build()
                        .register(new JohnzonProvider<>()) // we run in a remote tomee so we need to do it ourself
                        .target(url.toExternalForm()).path("api/hello")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(Hello.class)
                        .getName());
    }
}

