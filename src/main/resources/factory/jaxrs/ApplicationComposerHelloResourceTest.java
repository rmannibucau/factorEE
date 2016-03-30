package {{package}}.jaxrs;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.CdiExtensions;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@SimpleLog
@EnableServices("jaxrs")
@CdiExtensions(/* skip extensions since not needed in this test */)
@Classes(cdi = true, context = "app", value = {ApplicationConfig.class, HelloResource.class})
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
