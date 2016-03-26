package {{package}}.jpa;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ArquillianHelloEntityTest {
    @Deployment
    public static Archive<?> createDeploymentPackage() {
        return ShrinkWrap.create(WebArchive.class, "app.war")
                .addPackage(HelloEntity.class.getPackage())
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    @Test
    public void findByName() throws Exception { // Note: you can use arquillian-persistence for an advanced JPA integration
        // create some data
        userTransaction.begin();
        final HelloEntity helloEntity = new HelloEntity();
        helloEntity.setName("test");
        entityManager.persist(helloEntity);
        entityManager.flush();
        final long id = helloEntity.getId();
        userTransaction.commit();

        try { // do the test
            assertEquals(
                    1,
                    entityManager.createNamedQuery("HelloEntity.findByName")
                            .setParameter("name", "test")
                            .getResultList().size());
        } finally {

            // cleanup data
            userTransaction.begin();
            entityManager.remove(entityManager.getReference(HelloEntity.class, id));
            userTransaction.commit();
        }
    }
}
