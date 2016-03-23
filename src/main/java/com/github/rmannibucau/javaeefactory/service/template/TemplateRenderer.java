package com.github.rmannibucau.javaeefactory.service.template;

import com.github.rmannibucau.javaeefactory.service.IOService;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class TemplateRenderer {
    @Inject
    private IOService io;

    private final ConcurrentMap<String, Template> templates = new ConcurrentHashMap<>();

    public String render(final String template, final Object model) {
        return templates.computeIfAbsent(template, t -> {
            try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(template)) {
                return Mustache.compiler().compile(new String(io.read(is), StandardCharsets.UTF_8));
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        }).execute(model);
    }
}
