package bitronix.tm.integration.spring;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.sql.SQLException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import bitronix.tm.mock.events.ConnectionCloseEvent;
import bitronix.tm.mock.events.EventRecorder;
import bitronix.tm.mock.events.XAConnectionCloseEvent;
import bitronix.tm.resource.ResourceRegistrar;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class LifecycleTest {

    private static final Logger log = LoggerFactory.getLogger(LifecycleTest.class);
    private static final String DS_UNIQUE_NAME = "btm-spring-test-ds1";
    
    @Before @After
    public void clearEvents() {
        EventRecorder.clear();
    }

    @Test
    public void testLifecycle() throws SQLException {
        ClassPathXmlApplicationContext applicationContext = null;
        try {
            applicationContext = new ClassPathXmlApplicationContext("test-context.xml");

            // run a transactional bean method, making sure the minimum pool size (of 1) is exceeded
            verifyTransactionalMethod(applicationContext, 2);
    
            EventRecorder.clear();
    
            // make sure that a refresh closes all connections
            int totalPoolSize = getTotalPoolSize(applicationContext);
            assertEquals(3, totalPoolSize);

            applicationContext.refresh();
            
            verifyConnectionsClosed(totalPoolSize);
    
            // run the transactional bean method again after refresh
            EventRecorder.clear();
            verifyTransactionalMethod(applicationContext, 1);
        }
        finally {
            if (applicationContext != null) {
                applicationContext.close();
            }
        }
        assertNull(ResourceRegistrar.get(DS_UNIQUE_NAME));
    }

    private int getTotalPoolSize(ApplicationContext applicationContext) {
        int totalPoolSize = 0;
        for (PoolingDataSource ds : applicationContext.getBeansOfType(PoolingDataSource.class).values()) {
            if (!ds.getAllowLocalTransactions())
                totalPoolSize += ds.getTotalPoolSize();
        }
        return totalPoolSize;
    }

    private void verifyConnectionsClosed(int totalPoolSize) {
        if (log.isDebugEnabled()) {
            log.debug(EventRecorder.dumpToString());
        }

        Iterator<?> it = EventRecorder.iterateEvents();
        for (int i = 0; i < totalPoolSize; i++) {
            assertEquals(ConnectionCloseEvent.class, it.next().getClass());
            assertEquals(XAConnectionCloseEvent.class, it.next().getClass());
        }
        assertFalse(it.hasNext());
    }

    private void verifyTransactionalMethod(ApplicationContext applicationContext, int count) throws SQLException {
        assertNotNull(ResourceRegistrar.get(DS_UNIQUE_NAME));
        TransactionalBean bean = applicationContext.getBean(TransactionalBean.class);
        bean.doSomethingTransactional(count);
        bean.verifyEvents(count);
    }
}
