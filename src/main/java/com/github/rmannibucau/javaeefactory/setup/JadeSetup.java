package com.github.rmannibucau.javaeefactory.setup;

import com.github.rmannibucau.ohmyjs.servlet.DelegateFilterConfig;
import com.github.rmannibucau.ohmyjs.servlet.JadeServerRenderer;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@WebFilter(urlPatterns = "/js/app/template/*", asyncSupported = true)
public class JadeSetup extends JadeServerRenderer {
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final Map<String, String> overrides = new HashMap<>();
        final boolean isDev = "dev".equalsIgnoreCase(System.getProperty("javaeefactory.environment"));
        overrides.put("dev", Boolean.toString(isDev));
        overrides.put("active", Boolean.toString(isDev));
        overrides.put("sources", ofNullable(filterConfig.getServletContext().getRealPath("")).orElse("src/main/webapp"));
        overrides.put("mapToJade", "true");
        overrides.put("cache", "target/cache");
        overrides.put("includes", ".*\\.html");
        super.init(new DelegateFilterConfig(filterConfig, overrides));
    }
}
