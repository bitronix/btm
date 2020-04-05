package bitronix.tm.integration.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * @author aschoerk
 */
public abstract class SqlPersistenceFactory {
    @Inject
    TransactionManager tm;

    private final Logger logger = LoggerFactory.getLogger(SqlPersistenceFactory.class);

    private static final HashSet<String> PERSISTENCE_UNIT_NAMES = new HashSet<>();
    private EntityManagerFactory emf = null;

    protected DataSource ds;

    /**
     * allow to reset between Tests.
     */
    private static void clearPersistenceUnitNames() {
        PERSISTENCE_UNIT_NAMES.clear();
    }

    public abstract String getPersistenceUnitName();

    protected abstract DataSource createDataSource();


    public EntityManagerFactory getEmf() {
        return emf;
    }

    protected void createEntityManagerFactory() {
        if (emf == null) {
            ds = createDataSource();
            emf = Persistence.createEntityManagerFactory(getPersistenceUnitName());
        }
    }
    /**
     * prepare EntityManagerFactory
     */
    @PostConstruct
    public void construct() {
        logger.info("creating persistence factory {}", getPersistenceUnitName());
        synchronized (PERSISTENCE_UNIT_NAMES) {
            if (PERSISTENCE_UNIT_NAMES.contains(getPersistenceUnitName())) {
                throw new RuntimeException("Repeated construction of currently existing PersistenceFactory for " + getPersistenceUnitName());
            } else {
                createEntityManagerFactory();
                PERSISTENCE_UNIT_NAMES.add(getPersistenceUnitName());
            }
        }
    }

    /**
     * make sure all connections will be closed
     */
    @PreDestroy
    public void destroy() {
        logger.info("destroying persistence factory {}", getPersistenceUnitName());
        synchronized (PERSISTENCE_UNIT_NAMES) {
            if (!PERSISTENCE_UNIT_NAMES.contains(getPersistenceUnitName())) {
                throw new RuntimeException("Expected PersistenceFactory for " + getPersistenceUnitName());
            } else {
                if (emf != null && emf.isOpen()) {
                    emf.close();
                    emf = null;
                }
                PERSISTENCE_UNIT_NAMES.remove(getPersistenceUnitName());
            }
            clearPersistenceUnitNames();
        }
        if (ds != null) {
            try {
                Method closeMethod = ds.getClass().getMethod("close");
                closeMethod.invoke(ds);
            } catch (NoSuchMethodException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SqlPersistenceFactory that = (SqlPersistenceFactory) obj;

        return getPersistenceUnitName() != null ? getPersistenceUnitName().equals(that.getPersistenceUnitName())
                : that.getPersistenceUnitName() == null;
    }

    @Override
    public int hashCode() {
        return getPersistenceUnitName() != null ? getPersistenceUnitName().hashCode() : 0;
    }

    /**
     * returns EntityManager, to be injected and used so that the current threadSpecific context is correctly handled
     *
     * @return the EntityManager as it is returnable by producers.
     */
    public EntityManager produceEntityManager() {
        return new EntityManagerDelegate(this);
    }


    public EntityManager getTransactional(boolean expectTransaction) {
        return new TFrameStack().getEntityManager(this, expectTransaction);
    }

    public DataSource getDatasource() {
        return ds;
    }
}
