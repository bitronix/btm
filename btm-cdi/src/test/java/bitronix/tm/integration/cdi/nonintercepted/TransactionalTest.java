package bitronix.tm.integration.cdi.nonintercepted;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import bitronix.tm.integration.cdi.PlatformTransactionManager;
import bitronix.tm.integration.cdi.TransactionalCdiExtension;

/**
 * @author aschoerk
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({PlatformTransactionManager.class, Resources.class, TransactionalCdiExtension.class})
public class TransactionalTest {

    static Logger log = LoggerFactory.getLogger("testlogger");

    @BeforeClass
    public static void loginit() {
        log.info("log");
    }


    @Inject
    TransactionManager tm;

    @Inject
    Resources resources;

    @Before
    public void initContext() throws Exception {
    }

    @Test
    public void test() throws Exception {
        tm.begin();

        try (Connection connection = resources.getDs().getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("create table t (a varchar(200), b integer)");
                stmt.execute("insert into t (a, b) values ('a', 1 )");
                stmt.execute("insert into t (a, b) values ('b', 2 )");
                stmt.execute("select * from t");
                try (ResultSet result = stmt.getResultSet()) {
                    assert (result.next());
                    assert (result.next());
                    assert (!result.next());
                }
            }
        }
        tm.commit();
    }

    @Inject
    JPABean JPABean;

    @Test
    public void testTraMethod() throws Exception {

        JPABean.insertTestEntityInNewTra();
        Assert.assertEquals(1L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInRequired();
        tm.rollback();
        Assert.assertEquals(1L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(2L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTra();
        JPABean.insertTestEntityInNewTraAndRollback();
        tm.commit();
        Assert.assertEquals(4L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTraAndRollback();
        tm.commit();
        Assert.assertEquals(5L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInNewTraAndRollback();
        JPABean.insertTestEntityInRequired();
        tm.commit();
        Assert.assertEquals(6L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInNewTraAndRollback();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(7L, JPABean.countTestEntity());

        tm.begin();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTra();
        JPABean.insertTestEntityInNewTraAndSetRollbackOnly();
        tm.commit();
        Assert.assertEquals(9L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTraAndSetRollbackOnly();
        tm.commit();
        Assert.assertEquals(10L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInNewTraAndSetRollbackOnly();
        JPABean.insertTestEntityInRequired();
        tm.commit();
        Assert.assertEquals(11L, JPABean.countTestEntity());
        tm.begin();
        JPABean.insertTestEntityInNewTraAndSetRollbackOnly();
        JPABean.insertTestEntityInRequired();
        JPABean.insertTestEntityInNewTra();
        tm.rollback();
        Assert.assertEquals(12L, JPABean.countTestEntity());

    }

}
