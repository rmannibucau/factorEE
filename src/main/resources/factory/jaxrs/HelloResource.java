package {{package}}.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("hello")
public class HelloResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Hello hello(@QueryParam("name") final String name) {
        final Hello hello = new Hello();
        hello.setName(name == null ? "test" : name);
        return hello;
    }
}
