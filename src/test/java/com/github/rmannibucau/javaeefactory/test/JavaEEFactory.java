package com.github.rmannibucau.javaeefactory.test;

import lombok.Getter;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.RandomPort;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.servlet.ServletException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

// helper for tests, runner is reusable
@ContainerProperties({
        @ContainerProperties.Property(name = "javaeefactory.environment", value = "dev"),
        @ContainerProperties.Property(name = "openejb.jul.forceReload", value = "true")
})
public class JavaEEFactory {
    @RandomPort("http")
    private URL base;

    @Getter
    private String baseUrl;

    @PostConstruct
    private void initBase() throws MalformedURLException, ServletException {
        /* for JAXRS debugging
        final Bus bus = CxfUtil.getBus();
        bus.getInInterceptors().add(new LoggingInInterceptor());
        bus.getInFaultInterceptors().add(new LoggingInInterceptor());
        bus.getOutInterceptors().add(new LoggingOutInterceptor());
        bus.getOutFaultInterceptors().add(new LoggingOutInterceptor());
        */
        baseUrl = base.toExternalForm() + "javaeefactory/";
    }

    public WebTarget target() {
        return ClientBuilder.newBuilder().build().target(baseUrl + "api");
    }

    @Vetoed
    public static class Runner extends BlockJUnit4ClassRunner {
        public Runner(final Class<?> klass) throws InitializationError {
            super(klass);
        }

        private static final AtomicReference<Object> APP = new AtomicReference<>();
        private static final AtomicReference<Container> CONTAINER = new AtomicReference<>();
        private static final AtomicReference<Thread> HOOK = new AtomicReference<>();

        public static void close() {
            final Thread hook = HOOK.get();
            if (hook != null) {
                if (HOOK.compareAndSet(hook, null)) {
                    hook.run();
                }
            }
        }

        @Override
        protected List<MethodRule> rules(final Object test) {
            final List<MethodRule> rules = super.rules(test);
            rules.add((base1, method, target) -> new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    start();
                    composerInject(target);
                    base1.evaluate();
                }
            });
            return rules;
        }

        private static void start() throws Exception {
            if (CONTAINER.get() == null) {
                synchronized (JavaEEFactory.Runner.class) { // we can't use compareAndSet since we would create 2 containers potentially
                    if (CONTAINER.get() != null) {
                        return;
                    }

                    // setup the container config reading class annotation, using a randome http port and deploying the classpath
                    final Configuration configuration = new Configuration().randomHttpPort();
                    Stream.of(JavaEEFactory.class.getAnnotation(ContainerProperties.class).value())
                            .forEach(p -> configuration.property(p.name(), p.value()));
                    final Container container = new Container(configuration)
                            .deployClasspathAsWebApp("javaeefactory", new File("src/main/webapp"));
                    CONTAINER.compareAndSet(null, container);

                    // create app helper and inject basic values
                    final JavaEEFactory app = new JavaEEFactory();
                    app.base = new URL("http://localhost:" + configuration.getHttpPort() + "/");
                    app.initBase();
                    APP.set(app);
                    composerInject(app);

                    final Thread hook = new Thread() {
                        @Override
                        public void run() {
                            container.close();
                            CONTAINER.set(null);
                            APP.set(null);
                            try {
                                Runtime.getRuntime().removeShutdownHook(this);
                            } catch (final Exception e) {
                                // no-op: that's ok at that moment if not called manually
                            }
                        }
                    };
                    HOOK.set(hook);
                    Runtime.getRuntime().addShutdownHook(hook);
                }
            }
        }

        private static void composerInject(final Object target) throws IllegalAccessException { // bridge with app composer
            OWBInjector.inject(getBeanManager(), target, null);

            final Object app = APP.get();
            final Class<?> aClass = target.getClass();
            for (final Field f : aClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(RandomPort.class)) {
                    for (final Field field : app.getClass().getDeclaredFields()) {
                        if (field.getType() == f.getType()) {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            if (!f.isAccessible()) {
                                f.setAccessible(true);
                            }

                            final Object value = field.get(app);
                            f.set(target, value);
                            break;
                        }
                    }
                } else if (f.isAnnotationPresent(Application.class)) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    f.set(target, app);
                }
            }
            final Class<?> superclass = aClass.getSuperclass();
            if (superclass != Object.class) {
                composerInject(superclass);
            }
        }
    }

    private static BeanManagerImpl getBeanManager() {
        return WebBeansContext.currentInstance().getBeanManagerImpl();
    }
}
