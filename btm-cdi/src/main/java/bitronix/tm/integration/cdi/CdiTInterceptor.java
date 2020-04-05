package bitronix.tm.integration.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;

@Interceptor
@CdiTransactional
public class CdiTInterceptor {

    Logger logger = LoggerFactory.getLogger(CdiTInterceptor.class);

    @Inject
    TransactionManager tm;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        final Class<?> declaringClass = ctx.getMethod().getDeclaringClass();
        TFrameStack ts = new TFrameStack();
        TransactionInfo lastTransactionInfo = ts.topTransaction();
        Transactional.TxType attribute = null;
        Transactional classTransactional =
                declaringClass.getAnnotation(
                        Transactional.class);
        Transactional transactionMethod = ctx.getMethod().getAnnotation(Transactional.class);

        Class[] rollbackon = null;
        Class[] dontRollBackOn = null;

        if (transactionMethod != null) {
            attribute = transactionMethod.value();
            rollbackon = transactionMethod.rollbackOn();
            dontRollBackOn = transactionMethod.dontRollbackOn();

        } else if (classTransactional != null) {
            if (classTransactional != null) {
                attribute = classTransactional.value();
                rollbackon = classTransactional.rollbackOn();
                dontRollBackOn = classTransactional.dontRollbackOn();
            } else {
                attribute = Transactional.TxType.REQUIRED;
            }
        }
        if (attribute == null) {
            logger.error("CdiTransactionalInterceptor should not be used at this class: {}", declaringClass.getName());
            return ctx.proceed();
        } else {
            boolean passThroughRollbackException = true;
            try {
                logger.info("Thread {} L{} changing  from {} to {} xid: {} in {}.{}",
                        Thread.currentThread().getId(), ts.currentLevel(),
                        ts.currentTxType(),
                        attribute, MDC.get("XID"), declaringClass.getSimpleName(), ctx.getMethod().getName());
                ts.pushTransaction(attribute);
                return ctx.proceed();
            } catch (Throwable ex) {
                logger.info("Thread {} L{} Exception {} in {} xid: {} in {}.{}",
                        Thread.currentThread().getId(), ts.currentLevel(),
                        ex.getClass().getSimpleName(), attribute, MDC.get("XID"), declaringClass.getSimpleName(),
                        ctx.getMethod().getName());
                boolean doRollback = !isSubOrClassOfAny(ex.getClass(), dontRollBackOn)
                                     && (isSubOrClassOfAny(ex.getClass(), rollbackon) || ex instanceof RuntimeException);
                if (doRollback) {
                    passThroughRollbackException = false;
                    tm.rollback();
                }

                throw ex;
            } finally {
                logger.info("Thread {} L{} finally   in {} xid: {} in {}.{}",
                        Thread.currentThread().getId(), ts.currentLevel(), attribute, MDC.get("XID"), declaringClass.getSimpleName(),
                        ctx.getMethod().getName());
                try {
                    ts.popTransaction();
                } catch (RollbackException rbe) {
                    if (passThroughRollbackException) {
                        throw rbe;
                    }
                } finally {
                    logger.info("Thread {} L{} done      {} back to {} xid: {} in {}.{}",
                            Thread.currentThread().getId(), ts.topTransaction(), attribute,
                            lastTransactionInfo == null ? "undefined" : lastTransactionInfo.currentTxType,
                            MDC.get("XID"), declaringClass.getSimpleName(), ctx.getMethod().getName());
                }
            }
        }
    }

    public boolean isSubOrClassOfAny(Class c, Class[] classes) {
        for (Class clazz: classes) {
            if (clazz.isAssignableFrom(c)) {
                return true;
            }
        }
        return false;
    }

}