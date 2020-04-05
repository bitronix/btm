package bitronix.tm.integration.cdi;

import bitronix.tm.TransactionManagerServices;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @author aschoerk
 */
public class TUserTransaction implements UserTransaction {

    TransactionManager tm;

    TFrameStack ts;

    public TUserTransaction() {
        this.tm = TransactionManagerServices.getTransactionManager();
        this.ts = new TFrameStack();
    }

    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @exception NotSupportedException Thrown if the thread is already
     *    associated with a transaction and the Transaction Manager
     *    implementation does not support nested transactions.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public void begin() throws NotSupportedException, SystemException {
        if (ts.isUserTransaction())
            throw new IllegalStateException("Already UserTransaction running");
        ts.pushUserTransaction();
    }

    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     *
     * @exception RollbackException Thrown to indicate that
     *    the transaction has been rolled back rather than committed.
     *
     * @exception HeuristicMixedException Thrown to indicate that a heuristic
     *    decision was made and that some relevant updates have been committed
     *    while others have been rolled back.
     *
     * @exception HeuristicRollbackException Thrown to indicate that a
     *    heuristic decision was made and that all relevant updates have been
     *    rolled back.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to commit the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        try {
            if (!ts.isUserTransaction())
                throw new IllegalStateException("No UserTransaction");
            ts.commitTransaction();
        } catch (InvalidTransactionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return The transaction status. If no transaction is associated with
     *    the current thread, this method returns the Status.NoTransaction
     *    value.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public int getStatus() throws SystemException {
        return tm.getStatus();
    }

    /**
     * Get the transaction object that represents the transaction
     * context of the calling thread.
     *
     * @return the <code>Transaction</code> object representing the
     *	  transaction associated with the calling thread.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    public Transaction getTransaction() throws SystemException {
        return tm.getTransaction();
    }

    /**
     * Resume the transaction context association of the calling thread
     * with the transaction represented by the supplied Transaction object.
     * When this method returns, the calling thread is associated with the
     * transaction context specified.
     *
     * @param tobj The <code>Transaction</code> object that represents the
     *    transaction to be resumed.
     *
     * @exception InvalidTransactionException Thrown if the parameter
     *    transaction object contains an invalid transaction.
     *
     * @exception IllegalStateException Thrown if the thread is already
     *    associated with another transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     */
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        if (!ts.isUserTransaction())
            throw new IllegalStateException("No UserTransaction");
        tm.resume(tobj);
    }

    /**
     * Roll back the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a
     * transaction.
     *
     * @exception SecurityException Thrown to indicate that the thread is
     *    not allowed to roll back the transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        try {
            if (!ts.isUserTransaction())
                throw new IllegalStateException("No UserTransaction");
            ts.rollbackTransaction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (!ts.isUserTransaction())
            throw new IllegalStateException("No UserTransaction");
        tm.setRollbackOnly();
    }

    /**
     * Modify the timeout value that is associated with transactions started
     * by the current thread with the begin method.
     *
     * <p> If an application has not called this method, the transaction
     * service uses some default value for the transaction timeout.
     *
     * @param seconds The value of the timeout in seconds. If the value is zero,
     *        the transaction service restores the default value. If the value
     *        is negative a SystemException is thrown.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        tm.setTransactionTimeout(seconds);
    }

    /**
     * Suspend the transaction currently associated with the calling
     * thread and return a Transaction object that represents the
     * transaction context being suspended. If the calling thread is
     * not associated with a transaction, the method returns a null
     * object reference. When this method returns, the calling thread
     * is not associated with a transaction.
     *
     * @return Transaction object representing the suspended transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     *
     */
    public Transaction suspend() throws SystemException {
        if (!ts.isUserTransaction())
            throw new IllegalStateException("No UserTransaction");
        return tm.suspend();
    }
}
