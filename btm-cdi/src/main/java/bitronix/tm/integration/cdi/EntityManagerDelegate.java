package bitronix.tm.integration.cdi;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.Map;

/**
 * Used to delegate EntityManager actions to the current EntityManager of the Thread, as it is defined according
 * to Initialization and Transaction-Context
 * Created by aschoerk2 on 3/2/14.
 */
@SuppressWarnings("ClassWithTooManyMethods")
class EntityManagerDelegate implements EntityManager {

    private final SqlPersistenceFactory entityManagerStore;

    EntityManagerDelegate(SqlPersistenceFactory entityManagerStore) {
        this.entityManagerStore = entityManagerStore;
    }

    private EntityManager getEmbeddedEntityManager() {
        /*
         * make sure the transaction context is correctly started, if necessary, then return the workable EntityManager
         * of the thread.
         */
        try {
            return entityManagerStore.getTransactional(false);
        } catch (TransactionRequiredException e) {
            throw new RuntimeException("not expected exception: ", e);
        }
    }


    private EntityManager getEmbeddedEntityManager(boolean expectTransaction) {
        /*
         * make sure the transaction context is correctly started, if necessary, then return the workable EntityManager of the thread.
         */
        return entityManagerStore.getTransactional(expectTransaction);
    }

    @Override
    public void persist(final Object entity) {
        getEmbeddedEntityManager(true).persist(entity);
    }

    @Override
    public <T> T merge(final T entity) {
        return getEmbeddedEntityManager(true).merge(entity);
    }

    @Override
    public void remove(final Object entity) {
        getEmbeddedEntityManager(true).remove(entity);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey) {
        return getEmbeddedEntityManager().find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties) {
        return getEmbeddedEntityManager().find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode) {
        return getEmbeddedEntityManager().find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode,
            final Map<String, Object> properties) {
        return getEmbeddedEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(final Class<T> entityClass, final Object primaryKey) {
        return getEmbeddedEntityManager().getReference(entityClass, primaryKey);
    }

    @Override
    public void flush() {
        getEmbeddedEntityManager(true).flush();
    }

    @Override
    public FlushModeType getFlushMode() {
        return getEmbeddedEntityManager().getFlushMode();
    }

    @Override
    public void setFlushMode(final FlushModeType flushMode) {
        getEmbeddedEntityManager().setFlushMode(flushMode);
    }

    @Override
    public void lock(final Object entity, final LockModeType lockMode) {
        getEmbeddedEntityManager(true).lock(entity, lockMode);
    }

    @Override
    public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        getEmbeddedEntityManager(true).lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(final Object entity) {
        getEmbeddedEntityManager(true).refresh(entity);
    }

    @Override
    public void refresh(final Object entity, final Map<String, Object> properties) {
        getEmbeddedEntityManager(true).refresh(entity, properties);
    }

    @Override
    public void refresh(final Object entity, final LockModeType lockMode) {
        getEmbeddedEntityManager(true).refresh(entity, lockMode);
    }

    @Override
    public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        getEmbeddedEntityManager(true).refresh(entity, lockMode, properties);
    }

    @Override
    public void clear() {
        getEmbeddedEntityManager().clear();
    }

    @Override
    public void detach(final Object entity) {
        getEmbeddedEntityManager().detach(entity);
    }

    @Override
    public boolean contains(final Object entity) {
        return getEmbeddedEntityManager().contains(entity);
    }

    @Override
    public LockModeType getLockMode(final Object entity) {
        return getEmbeddedEntityManager(true).getLockMode(entity);
    }

    @Override
    public void setProperty(final String propertyName, final Object value) {
        getEmbeddedEntityManager().setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return getEmbeddedEntityManager().getProperties();
    }

    @Override
    public Query createQuery(final String qlString) {
        return getEmbeddedEntityManager().createQuery(qlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
        return getEmbeddedEntityManager().createQuery(criteriaQuery);
    }

    @Override
    public Query createQuery(CriteriaUpdate criteriaUpdate) {
        return getEmbeddedEntityManager().createQuery(criteriaUpdate);
    }

    @Override
    public Query createQuery(CriteriaDelete criteriaDelete) {
        return getEmbeddedEntityManager().createQuery(criteriaDelete);
    }

    @Override
    public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
        return getEmbeddedEntityManager().createQuery(qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(final String name) {
        return getEmbeddedEntityManager().createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
        return getEmbeddedEntityManager().createNamedQuery(name, resultClass);
    }

    @Override
    public Query createNativeQuery(final String sqlString) {
        return getEmbeddedEntityManager().createNativeQuery(sqlString);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Query createNativeQuery(final String sqlString, final Class resultClass) {
        return getEmbeddedEntityManager().createNativeQuery(sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
        return getEmbeddedEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return getEmbeddedEntityManager().createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return getEmbeddedEntityManager().createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class[] resultClasses) {
        return getEmbeddedEntityManager().createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return getEmbeddedEntityManager().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        getEmbeddedEntityManager().joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return getEmbeddedEntityManager().isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(final Class<T> cls) {
        return getEmbeddedEntityManager().unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return getEmbeddedEntityManager().getDelegate();
    }

    @Override
    public void close() {
        getEmbeddedEntityManager().close();
    }

    @Override
    public boolean isOpen() {
        return getEmbeddedEntityManager().isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return null;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getEmbeddedEntityManager().getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEmbeddedEntityManager().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getEmbeddedEntityManager().getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return getEmbeddedEntityManager().createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        return getEmbeddedEntityManager().createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        return getEmbeddedEntityManager().getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return getEmbeddedEntityManager().getEntityGraphs(entityClass);
    }

}
