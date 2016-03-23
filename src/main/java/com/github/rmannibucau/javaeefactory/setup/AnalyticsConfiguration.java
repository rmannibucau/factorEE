package com.github.rmannibucau.javaeefactory.setup;

import com.github.rmannibucau.javaeefactory.configuration.Configuration;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AnalyticsConfiguration {
    @Inject
    @Getter
    @Configuration("javaee-factory.analytics.code")
    private String code;
}
