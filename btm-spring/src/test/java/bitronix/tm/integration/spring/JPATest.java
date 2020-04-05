package bitronix.tm.integration.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * @author aschoerk
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JPATest {

    @Resource(name = "h2DataSource")
    private DataSource dataSource;

    @Inject
    TransactionalJPABean transactionalJPABean;

    @Test
    public void test() {
        transactionalJPABean.insertTestEntityInNewTra();
        transactionalJPABean.insertTestEntityInRequired();
    }
}
