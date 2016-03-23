package com.github.rmannibucau.javaeefactory.setup;

import com.github.rmannibucau.ohmyjs.servlet.BabelJsServerTranspiler;
import com.github.rmannibucau.ohmyjs.servlet.DelegateFilterConfig;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

@WebFilter(urlPatterns = "/js/app/*", asyncSupported = true)
public class BabelSetup extends BabelJsServerTranspiler {
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final Map<String, String> overrides = new HashMap<>();
        final boolean isDev = "dev".equalsIgnoreCase(System.getProperty("javaeefactory.environment"));
        overrides.put("dev", Boolean.toString(isDev));
        overrides.put("active", Boolean.toString(isDev));
        overrides.put("templates", "../template");
        overrides.put("templateExtension", "jade");
        overrides.put("sources", ofNullable(filterConfig.getServletContext().getRealPath("")).orElse("src/main/webapp"));
        overrides.put("module", "amd");
        overrides.put("mapToEs6", "true");
        overrides.put("cache", "target/cache");
        overrides.put("includes", ".*\\.js");
        super.init(new DelegateFilterConfig(filterConfig, overrides));
    }
}
