package bitronix.tm.integration.cdi.cdiintercepted;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.integration.cdi.CdiTInterceptor;
import bitronix.tm.integration.cdi.EjbTInterceptor;
import bitronix.tm.integration.cdi.PlatformTransactionManager;
import bitronix.tm.integration.cdi.TransactionalCdiExtension;

/**
 * @author aschoerk
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({
        H2PersistenceFactory.class,
        CdiTInterceptor.class, TransactionalCdiExtension.class})
@AdditionalPackages(PlatformTransactionManager.class)
public class H2CdiTransactionalTest {

    static Logger log = LoggerFactory.getLogger("testlogger");

    @BeforeClass
    public static void loginit() {
        log.info("log");
    }

    @Inject
    PlatformTransactionManager platformTransactionManager;

    @Inject
    UserTransaction tm;

    @Inject
    DataSource dataSource;

    @Before
    public void initContext() throws Exception {
    }

    @Inject
    CDITransactionalJPABean jpaBean;

    @Test
    public void testTraMethod() throws Exception {

        jpaBean.insertTestEntityInNewTra();
        Assert.assertEquals(1L, jpaBean.countTestEntity());
        tm.begin();
        jpaBean.insertTestEntityInRequired();
        tm.rollback();
        Assert.assertEquals(1L, jpaBean.countTestEntity());
        tm.begin();
        jpaBean.insertTestEntityInRequired();
        jpaBean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(2L, jpaBean.countTestEntity());
        tm.begin();
        jpaBean.insertTestEntityInRequired();
        jpaBean.insertTestEntityInNewTra();
        insertTestEntityInNewTraAndRollback();
        tm.commit();
        Assert.assertEquals(4L, jpaBean.countTestEntity());
        tm.begin();
        jpaBean.insertTestEntityInRequired();
        insertTestEntityInNewTraAndRollback();
        tm.commit();
        Assert.assertEquals(5L, jpaBean.countTestEntity());
        tm.begin();
        insertTestEntityInNewTraAndRollback();
        jpaBean.insertTestEntityInRequired();
        tm.commit();
        Assert.assertEquals(6L, jpaBean.countTestEntity());
        tm.begin();
        insertTestEntityInNewTraAndRollback();
        jpaBean.insertTestEntityInRequired();
        jpaBean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(7L, jpaBean.countTestEntity());

        tm.begin();
        jpaBean.insertTestEntityInRequired();
        jpaBean.insertTestEntityInNewTra();
        insertTestEntityInNewTraAndSetRollbackOnly();
        tm.commit();
        Assert.assertEquals(9L, jpaBean.countTestEntity());
        tm.begin();
        jpaBean.insertTestEntityInRequired();
        insertTestEntityInNewTraAndSetRollbackOnly();
        tm.commit();
        Assert.assertEquals(10L, jpaBean.countTestEntity());
        tm.begin();
        insertTestEntityInNewTraAndSetRollbackOnly();
        jpaBean.insertTestEntityInRequired();
        tm.commit();
        Assert.assertEquals(11L, jpaBean.countTestEntity());
        tm.begin();
        insertTestEntityInNewTraAndSetRollbackOnly();
        jpaBean.insertTestEntityInRequired();
        jpaBean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(12L, jpaBean.countTestEntity());

    }

    private void insertTestEntityInNewTraAndSetRollbackOnly() throws Exception {
        try {
            jpaBean.insertTestEntityInNewTraAndSetRollbackOnly();
            Assert.fail("Expected Rollbackexception during commit of new tra");
        } catch (RollbackException ex){

        }
    }

    private void insertTestEntityInNewTraAndRollback() throws Exception {
        try {
            jpaBean.insertTestEntityInNewTraAndSetRollbackOnly();
            Assert.fail("expected rollbackexception");
        }
        catch (RollbackException rbe) {

        }
    }

}
