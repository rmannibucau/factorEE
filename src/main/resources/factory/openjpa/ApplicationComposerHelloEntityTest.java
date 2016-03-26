package {{package}}.jpa;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Classes(cdi = true, value = HelloEntity.class /* to find by scanning the entity */)
@PersistenceUnitDefinition // activates JPA
@RunWith(ApplicationComposer.class)
public class ApplicationComposerHelloEntityTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    private long id;

    @Before
    public void before() throws Exception {
        userTransaction.begin();
        final HelloEntity helloEntity = new HelloEntity();
        helloEntity.setName("test");
        entityManager.persist(helloEntity);
        entityManager.flush();
        id = helloEntity.getId();
        userTransaction.commit();
    }

    @After
    public void after() throws Exception {
        userTransaction.begin();
        entityManager.remove(entityManager.getReference(HelloEntity.class, id));
        userTransaction.commit();
    }

    @Test
    public void sayHelloToTheDefaultUser() {
        assertEquals(
                1,
                entityManager.createNamedQuery("HelloEntity.findByName")
                        .setParameter("name", "test")
                        .getResultList().size());
    }
}
