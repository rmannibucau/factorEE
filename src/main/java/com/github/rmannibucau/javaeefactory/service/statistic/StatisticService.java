package com.github.rmannibucau.javaeefactory.service.statistic;

import com.github.rmannibucau.javaeefactory.configuration.Configuration;
import com.github.rmannibucau.javaeefactory.jpa.Statistic;
import com.github.rmannibucau.javaeefactory.service.event.CreateProject;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.util.Optional.ofNullable;

@ApplicationScoped // TODO: querying by interval (day, month, year?)
public class StatisticService {
    private static final String PROJECT_STAT_NAME = "Total Project Count";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(final CreateProject project) {
        increment(PROJECT_STAT_NAME);
        project.getFacets().forEach(this::increment);
        entityManager.flush();
    }

    // save stats by day
    private void increment(final String name) {
        final Statistic.TemporalId id = new Statistic.TemporalId();
        id.setName(name);
        id.setDate(new Date());

        ofNullable(entityManager.find(Statistic.class, id))
                .orElseGet(() -> {
                    final Statistic statistic = new Statistic();
                    statistic.setId(id);
                    entityManager.persist(statistic);
                    return statistic;
                }).increment();
    }

    @ApplicationScoped
    public static class ProjectListener {
        @Inject
        private StatisticService statistics;

        @Resource(name = "thread/statisticsExecutor")
        private ManagedExecutorService executorService;

        @Inject
        @Configuration("${javaee-factory.statistics.retries:3}")
        private Integer retries;

        @Inject
        @Configuration("${javaee-factory.statistics.retry-sleep:250}")
        private Integer retrySleep;

        @Inject
        @Configuration("${javaee-factory.statistics.shutdownTimeout:50000}")
        private Integer shutdownTimeout;

        private volatile boolean skip = false;
        private final AtomicInteger inProgressTasks = new AtomicInteger();

        // don't block to return ASAP to the client, not very important if it fails for the end user
        void capture(@Observes final CreateProject createProject) {
            if (skip) {
                return;
            }
            executorService.submit(() -> {
                inProgressTasks.incrementAndGet();
                for (int i = 0; i < retries; i++) {
                    try {
                        statistics.save(createProject);
                        return;
                    } catch (final Exception te) { // if an optimistic lock or constraint error was the cause, just retry
                        Throwable e = te.getCause();
                        while (e != null
                                && !OptimisticLockException.class.isInstance(e)
                                && !SQLException.class.isInstance(e)) {
                            if (e.getCause() == e) {
                                e = null;
                                break;
                            }
                            e = e.getCause();
                        }
                        if (e != null) {
                            if (retries - 1 == i) { // no need to retry
                                failed(createProject);
                                throw RuntimeException.class.isInstance(e) ? RuntimeException.class.cast(e) : new IllegalStateException(e);
                            }

                            if (retrySleep > 0) {
                                try {
                                    sleep(retrySleep);
                                } catch (final InterruptedException ie) {
                                    Thread.interrupted();
                                    break;
                                }
                            }
                        }
                    } finally {
                        inProgressTasks.decrementAndGet();
                    }
                }
                // we shouldn't come there so warn
                failed(createProject);
            });
        }

        private void failed(final CreateProject createProject) {
            Logger.getLogger(StatisticService.class.getName())
                    .warning("Can't save statistics of " + createProject + " in " + retries + " retries.");
        }

        @PreDestroy
        private void tryToSaveCurrentTasks() {
            skip = true;
            final long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < shutdownTimeout && inProgressTasks.get() > 0) {
                try {
                    sleep(250);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                    return;
                }
            }
            if (inProgressTasks.get() > 0) {
                Logger.getLogger(StatisticService.class.getName())
                        .warning("Some task were pending when shutting down: " + inProgressTasks.get());
            }
        }
    }
}
