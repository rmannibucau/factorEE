package {{package}}.deltaspike;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Jars("deltaspike-")
@Classes(cdi = true, value = ApplicationConfiguration.class)
@RunWith(ApplicationComposer.class)
public class ApplicationComposerConfigurationTest {
    @Inject
    private ApplicationConfiguration configuration;

    @Test
    public void checkApiVersion() {
        assertEquals("1.0", configuration.getApiVersion());
    }
}
