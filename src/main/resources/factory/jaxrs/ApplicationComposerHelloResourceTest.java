package {{package}}.jaxrs;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Default
@EnableServices("jaxrs")
@Classes(cdi = true, context = "app")
@RunWith(ApplicationComposer.class)
public class ApplicationComposerHelloResourceTest {
    @RandomPort("http")
    private URL base;

    @Test
    public void sayHelloToTheDefaultUser() {
        assertEquals(
                "test",
                ClientBuilder.newBuilder().build()
                        .target(base.toExternalForm()).path("app/api/hello")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(Hello.class)
                        .getName());
    }
}
