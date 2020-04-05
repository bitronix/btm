package bitronix.tm.integration.cdi.nonintercepted;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.sql.Connection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.integration.cdi.PoolingDataSourceFactoryBean;
import bitronix.tm.mock.events.EventRecorder;
import bitronix.tm.mock.events.XAResourceCommitEvent;
import bitronix.tm.mock.events.XAResourceEndEvent;
import bitronix.tm.mock.events.XAResourcePrepareEvent;
import bitronix.tm.mock.events.XAResourceStartEvent;

public class TransactionalBean {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionalBean.class);
    
    @Inject
    private PoolingDataSourceFactoryBean dataSource;

    @Inject
    TransactionManager transactionManager;

    public void doSomethingTransactional(int count) throws Exception {
        transactionManager.begin();
        log.info("From transactional method, claiming {} connection(s)", count);

        Connection[] connections = new Connection[count];
        try {
            for (int i = 0; i < count; i++) {
                connections[i] = dataSource.getObject().getConnection();
                connections[i].createStatement();
            }
        } finally {
            for (int i = 0; i < count; i++) {
                if (connections[i] != null) {
                    connections[i].close();
                }
            }
        }
        transactionManager.commit();
    }

    public void verifyEvents(int count) {
        if (log.isDebugEnabled()) {
            log.debug(EventRecorder.dumpToString());
        }
        
        Iterator<?> it = EventRecorder.iterateEvents();
        
        for (int i = 0; i < count; i++) {
            assertEquals(XAResource.TMNOFLAGS, ((XAResourceStartEvent) it.next()).getFlag());
        }
        for (int i = 0; i < count; i++) {
            assertEquals(XAResource.TMSUCCESS, ((XAResourceEndEvent) it.next()).getFlag());
        }
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                assertEquals(XAResource.XA_OK, ((XAResourcePrepareEvent) it.next()).getReturnCode());
            }
        }
        for (int i = 0; i < count; i++) {
            assertEquals(count == 1, ((XAResourceCommitEvent) it.next()).isOnePhase());
        }
        
        assertFalse(it.hasNext());
    }
}
