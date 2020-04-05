package bitronix.tm.integration.spring;

import bitronix.tm.mock.events.EventRecorder;
import bitronix.tm.mock.events.XAResourceCommitEvent;
import bitronix.tm.mock.events.XAResourceEndEvent;
import bitronix.tm.mock.events.XAResourcePrepareEvent;
import bitronix.tm.mock.events.XAResourceStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class TransactionalJPABean {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionalJPABean.class);
    
    @Resource(name = "h2DataSource")
    private DataSource dataSource;


    @PersistenceContext(name = "btm-cdi-test-h2-pu")
    private EntityManager em;

    @Inject
    LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    EntityManager getEm() {
        return em;
    }

    @Transactional
    public void doSomethingTransactional(int count) throws SQLException {
        log.info("From transactional method, claiming {} connection(s)", count);

        Connection[] connections = new Connection[count];
        try {
            for (int i = 0; i < count; i++) {
                connections[i] = dataSource.getConnection();
                connections[i].createStatement();
            }
        } finally {
            for (int i = 0; i < count; i++) {
                if (connections[i] != null) {
                    connections[i].close();
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertTestEntityInNewTra() {
        getEm().persist(new TestEntity1());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertTestEntityInRequired() {
        getEm().persist(new TestEntity1());
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
