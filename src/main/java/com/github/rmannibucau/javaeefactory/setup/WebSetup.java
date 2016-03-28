package com.github.rmannibucau.javaeefactory.setup;

import com.github.rmannibucau.ohmyjs.servlet.BabelJsServerTranspiler;
import com.github.rmannibucau.ohmyjs.servlet.DelegateFilterConfig;
import com.github.rmannibucau.ohmyjs.servlet.JadeServerRenderer;

import javax.servlet.DispatcherType;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Optional.ofNullable;

public class WebSetup implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> c, final ServletContext ctx) throws ServletException {
        final boolean isDev = "dev".equalsIgnoreCase(System.getProperty("javaeefactory.environment"));
        if (!isDev) {
            return;
        }

        final FilterRegistration.Dynamic babeljs = ctx.addFilter("babeljs", BabelSetup.class);
        babeljs.setAsyncSupported(true);
        babeljs.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/js/app/*");

        final FilterRegistration.Dynamic jade = ctx.addFilter("jade", JadeSetup.class);
        jade.setAsyncSupported(true);
        jade.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/js/app/template/*");
    }

    public static class BabelSetup extends BabelJsServerTranspiler {
        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            final Map<String, String> overrides = new HashMap<>();
            overrides.put("dev", "true");
            overrides.put("active", "true");
            overrides.put("templates", "../template");
            overrides.put("templateExtension", "jade");
            overrides.put("sources", ofNullable(filterConfig.getServletContext().getRealPath("")).orElse("src/main/webapp"));
            overrides.put("module", "amd");
            overrides.put("mapToEs6", "true");
            overrides.put("cache", "target/cache");
            overrides.put("includes", ".*\\.js");
            super.init(new DelegateFilterConfig(filterConfig, overrides));
        }

        @Override
        public void destroy() {
            // no-op
        }
    }

    public static class JadeSetup extends JadeServerRenderer {
        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            final Map<String, String> overrides = new HashMap<>();
            overrides.put("dev", "true");
            overrides.put("active", "true");
            overrides.put("sources", ofNullable(filterConfig.getServletContext().getRealPath("")).orElse("src/main/webapp"));
            overrides.put("mapToJade", "true");
            overrides.put("cache", "target/cache");
            overrides.put("includes", ".*\\.html");
            super.init(new DelegateFilterConfig(filterConfig, overrides));
        }

        @Override
        public void destroy() {
            // no-op
        }
    }
}
