package bitronix.tm.integration.cdi.cdiintercepted;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.util.Iterator;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.integration.cdi.entities.TestEntity1;
import bitronix.tm.mock.events.EventRecorder;
import bitronix.tm.mock.events.XAResourceCommitEvent;
import bitronix.tm.mock.events.XAResourceEndEvent;
import bitronix.tm.mock.events.XAResourcePrepareEvent;
import bitronix.tm.mock.events.XAResourceStartEvent;

@Transactional
public class CDITransactionalJPABean {
    
    private static final Logger log = LoggerFactory.getLogger(CDITransactionalJPABean.class);

    @Resource(name = "h2DataSource")
    private DataSource dataSource;

    @Inject
    TransactionManager tm;

    @Inject
    EntityManager em;


    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void insertTestEntityInNewTra() throws Exception {
        em.persist(new TestEntity1());
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void insertTestEntityInNewTraAndSetRollbackOnly() throws Exception {
        em.persist(new TestEntity1());
        tm.setRollbackOnly();
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public void insertTestEntityInRequired() throws Exception {
        em.persist(new TestEntity1());
    }

    public long countTestEntity() throws Exception {
        Long result = em.createQuery("select count(e) from TestEntity1 e", Long.class).getSingleResult();
        return result;
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
