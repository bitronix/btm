package bitronix.tm.integration.cdi;

import bitronix.tm.TransactionManagerServices;

import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.transaction.*;

/**
 * The logic necessary to handle stacking of transactions according ejb-transactionattributetypes.
 * Later: should be able to handle bean managed transactions as well (UserTransaction).
 *
 * @author aschoerk
 */
public class TFrameStack {

    private static ThreadLocal<TransactionInfo> transactionInfoThreadLocal = new ThreadLocal<>();

    private final TransactionManager tm = TransactionManagerServices.getTransactionManager();

    public TransactionInfo topTransaction() {
        return transactionInfoThreadLocal.get();
    }

    public int currentLevel() {
        TransactionInfo tt = topTransaction();
        return tt == null ? 0 : tt.level;
    }

    public TransactionAttributeType currentType() {
        TransactionInfo tt = topTransaction();
        return tt == null ? null : tt.currentTransactionAttributeType;
    }

    public Transactional.TxType currentTxType() {
        TransactionInfo tt = topTransaction();
        return tt == null ? null : tt.currentTxType;
    }

    public void commitTransaction() throws HeuristicRollbackException, RollbackException, InvalidTransactionException, HeuristicMixedException, SystemException {
        popTransaction(true, false);
    }
    public void rollbackTransaction() throws HeuristicRollbackException, RollbackException, InvalidTransactionException, HeuristicMixedException, SystemException {
        popTransaction(true, true);
    }

    public void popTransaction() throws HeuristicRollbackException, RollbackException, InvalidTransactionException, HeuristicMixedException, SystemException {
        popTransaction(false, false);
    }

    public boolean isUserTransaction() {
        final TransactionInfo transactionInfo = transactionInfoThreadLocal.get();
        return transactionInfo != null ? transactionInfo.userTransaction : false;
    }


    public void popTransaction(boolean expectNewTra, boolean rollback) throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, InvalidTransactionException {
        TransactionInfo transactionInfo = transactionInfoThreadLocal.get();
        transactionInfoThreadLocal.set(transactionInfo.previous);
        try {
            if (expectNewTra && !transactionInfo.newTra)
                throw new IllegalStateException("expected new tra-transaction-frame on stack");
            if (transactionInfo.newTra || transactionInfo.suspended != null) {
                if (rollback)
                    tm.rollback();
                else
                    tm.commit();
                for (EntityManagerInfo ei: transactionInfo.entityManagers) {
                    ei.em.close();
                }
            }
        }
        finally {
            if (transactionInfo.suspended != null) {
                tm.resume(transactionInfo.suspended);
            }
        }
    }

    public void pushUserTransaction() throws SystemException, NotSupportedException {
        final TransactionInfo previousTransactionInfo = topTransaction();
        TransactionInfo transactionInfo = new TransactionInfo(previousTransactionInfo);
        transactionInfo.currentTransactionAttributeType = null;
        if (traActive()) {
            transactionInfo.suspended = tm.suspend();
        }
        tm.begin();
        transactionInfo.newTra = true;
        transactionInfo.setUserTransaction();
        transactionInfoThreadLocal.set(transactionInfo);

    }

    public void pushTransaction(Transactional.TxType attributeType) throws SystemException, NotSupportedException {
        final TransactionInfo previousTransactionInfo = topTransaction();
        TransactionInfo transactionInfo = new TransactionInfo(previousTransactionInfo);
        transactionInfo.currentTxType = attributeType;
        switch (attributeType) {
            case MANDATORY:
                if (!traActive())
                    throw new TransactionRequiredException("Mandatory Transaction");
                break;
            case REQUIRED:
                if (!traActive()) {
                    tm.begin();
                    transactionInfo.newTra = true;
                }
                break;
            case REQUIRES_NEW:
                if (traActive()) {
                    transactionInfo.suspended = tm.suspend();
                }
                tm.begin();
                transactionInfo.newTra = true;
                break;
            case SUPPORTS:
                break;
            case NOT_SUPPORTED:
                if (traActive()) {
                    transactionInfo.suspended = tm.suspend();
                }
                break;
            case NEVER:
                if (traActive())
                    throw new TransactionRequiredException("Transaction is not allowed");
                break;
        }
        transactionInfoThreadLocal.set(transactionInfo);
    }

    public void pushTransaction(TransactionAttributeType attributeType) throws SystemException, NotSupportedException {

        final TransactionInfo previousTransactionInfo = topTransaction();
        TransactionInfo transactionInfo = new TransactionInfo(previousTransactionInfo);
        transactionInfo.currentTransactionAttributeType = attributeType;
        switch (attributeType) {
            case MANDATORY:
                if (!traActive())
                    throw new TransactionRequiredException("Mandatory Transaction");
                break;
            case REQUIRED:
                if (!traActive()) {
                    tm.begin();
                    transactionInfo.newTra = true;
                }
                break;
            case REQUIRES_NEW:
                if (traActive()) {
                    transactionInfo.suspended = tm.suspend();
                }
                tm.begin();
                transactionInfo.newTra = true;
                break;
            case SUPPORTS:
                break;
            case NOT_SUPPORTED:
                if (traActive()) {
                    transactionInfo.suspended = tm.suspend();
                }
                break;
            case NEVER:
                if (traActive())
                    throw new TransactionRequiredException("Transaction is not allowed");
                break;
        }
        transactionInfoThreadLocal.set(transactionInfo);
    }

    private boolean traActive() {
        try {
            return tm.getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            throw new RuntimeException("simulated ejb", e);
        }
    }

    public EntityManager getEntityManager(SqlPersistenceFactory sqlPersistenceFactory, boolean expectTransaction) {
        if (expectTransaction && !traActive()) {
            throw new TransactionRequiredException("ejb simulation");
        }
        String name = sqlPersistenceFactory.getPersistenceUnitName();
        TransactionInfo info = topTransaction();
        while (info != null) {
            if (info.newTra || info.suspended != null)
                break;
            info = info.previous;
        }
        if (info != null) {
            for (EntityManagerInfo ei: info.entityManagers) {
                if (ei.persistenceUnit.equals(name)) {
                    return ei.em;
                }
            }
            EntityManager result = sqlPersistenceFactory.getEmf().createEntityManager();
            if (traActive()) {
                result.joinTransaction();
            }
            return result;
        } else {
            assert !traActive();
            return sqlPersistenceFactory.getEmf().createEntityManager();
        }
    }
}
