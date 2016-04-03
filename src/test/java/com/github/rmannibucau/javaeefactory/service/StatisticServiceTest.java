package com.github.rmannibucau.javaeefactory.service;

import com.github.rmannibucau.javaeefactory.service.domain.ProjectRequest;
import com.github.rmannibucau.javaeefactory.test.JavaEEFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JavaEEFactory.Runner.class)
public class StatisticServiceTest {
    @Inject
    private ProjectGenerator generator;

    @Inject
    private UserTransaction userTransaction;

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    @After
    public void cleanDb() throws Exception {
        userTransaction.begin();
        try {
            entityManager.createQuery("delete from Statistic").executeUpdate();
        } finally {
            if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                userTransaction.commit();
            }
        }
    }

    @Test
    public void emptyFacetStats() {
        generate(emptyList());
        assertCounters(new HashMap<String, Integer>() {{
            put("Total Project Count", 1);
        }});
    }

    @Test
    public void facetStats() {
        generate(asList("JAX-RS", "Lombok", "ApplicationComposer"));
        generate(singletonList("JAX-RS"));
        generate(singletonList("Arquillian"));
        generate(asList("Arquillian", "JAX-RS"));
        assertCounters(new HashMap<String, Integer>() {{
            put("Total Project Count", 4);
            put("ApplicationComposer", 1);
            put("Arquillian", 2);
            put("Lombok", 1);
            put("JAX-RS", 3);
        }});
    }

    private void assertCounters(final Map<String, Integer> counters) {
        for (int i = 0; i < 10; i++) {
            try {
                assertEquals(counters, statsToMap());

                // we have the counts, check we created a single counter by facet - test is fast enough to be executed on a single day
                // ie we are able to aggregate per day
                entityManager.createQuery("select s.id.name, count(s.total) from Statistic s group by s.id.name")
                        .getResultList().stream()
                        .map(o -> Number.class.cast(Object[].class.cast(o)[1]).intValue())
                        .forEach(v -> assertEquals(1, v));

                return;
            } catch (final AssertionError error) {
                // retry
                try {
                    sleep(500);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                    break;
                }
            }
        }
        fail("Can't find statistics expected=" + counters + ", actual= " + statsToMap());
    }

    private Object statsToMap() {
        return entityManager.createQuery("select s.id.name, sum(s.total) from Statistic s group by s.id.name").getResultList().stream()
                .collect(toMap(o -> Object[].class.cast(o)[0].toString(), o -> Number.class.cast(Object[].class.cast(o)[1]).intValue()));
    }

    private void generate(final Collection<String> facets) {
        generator.generate(new ProjectRequest("Maven", new ProjectRequest.BuildConfiguration("test", "d", "war", "g", "a", "1", "1.8"), "base", facets), new ByteArrayOutputStream());
    }
}
