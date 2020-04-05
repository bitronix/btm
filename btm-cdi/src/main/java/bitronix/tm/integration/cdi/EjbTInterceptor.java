package bitronix.tm.integration.cdi;

/**
 * @author aschoerk
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

@Interceptor
@EjbTransactional
public class EjbTInterceptor {

    private final Logger logger =
            LoggerFactory.getLogger(EjbTInterceptor.class);

    @Inject
    TransactionManager tm;



    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {

        final Class<?> declaringClass = ctx.getMethod().getDeclaringClass();
        Class<?> targetClass = getTargetClass(ctx);
        boolean beanManaged = isBeanManaged(declaringClass) || isBeanManaged(targetClass);
        TFrameStack ts = new TFrameStack();
        TransactionInfo lastTransactionInfo = ts.topTransaction();

            TransactionAttributeType attribute;
            if (beanManaged) {
                attribute = TransactionAttributeType.NOT_SUPPORTED;
            } else {
                TransactionAttribute transaction =
                        declaringClass.getAnnotation(
                                TransactionAttribute.class);
                TransactionAttribute transactionMethod = ctx.getMethod().getAnnotation(TransactionAttribute.class);

                if (transactionMethod != null) {
                    attribute = transactionMethod.value();
                } else if (transaction != null) {
                    attribute = transaction.value();
                } else {
                    attribute = TransactionAttributeType.REQUIRED;
                }
            }


            boolean passThroughRollbackException = true;
            try {
                logger.info("Thread {} L{} changing  from {} to {} xid: {} in {}.{}",
                        Thread.currentThread().getId(), ts.currentLevel(),
                        ts.currentType(),
                        attribute, MDC.get("XID"), declaringClass.getSimpleName(), ctx.getMethod().getName());
                ts.pushTransaction(attribute);
                return ctx.proceed();
            } catch (Throwable ex) {
                logger.info("Thread {} L{} Exception {} in {} xid: {} in {}.{}",
                        Thread.currentThread().getId(), ts.currentLevel(),
                        ex.getClass().getSimpleName(), attribute, MDC.get("XID"), declaringClass.getSimpleName(),
                        ctx.getMethod().getName());
                if (beanManaged) {
                    if (ex instanceof RuntimeException) {
                        throw new EJBException((RuntimeException) ex);
                    } else {
                        throw ex;
                    }
                }
                ApplicationException applicationException = findApplicationException(ex);
                boolean doRollback =
                        applicationException != null ? applicationException.rollback() : ex instanceof RuntimeException;

                if (doRollback) {
                    passThroughRollbackException = false;
                    tm.rollback();
                }

                if (applicationException == null && ex instanceof RuntimeException) {
                    throw new EJBException((RuntimeException) ex);
                } else {
                    throw ex;
                }
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
                            lastTransactionInfo == null ? "undefined" : lastTransactionInfo.currentTransactionAttributeType,
                            MDC.get("XID"), declaringClass.getSimpleName(), ctx.getMethod().getName());
                }
            }
        }


    private boolean traActive() throws SystemException {
        return tm.getStatus() != Status.STATUS_NO_TRANSACTION;
    }

    private Class<?> getTargetClass(InvocationContext ctx) {
        final Object target = ctx.getTarget();
        if (target == null)
            return null;
        Class<? extends Object> res = target.getClass();
        if (res.getName().endsWith("WeldSubclass"))
            return res.getSuperclass();
        else
            return res;

    }

    private ApplicationException findApplicationException(Throwable ex) {
        // search for applicationexception
        Class<?> tmp = ex.getClass();
        ApplicationException applicationException = null;
        while (!tmp.equals(Throwable.class)) {
            applicationException = tmp.getAnnotation(ApplicationException.class);
            if (applicationException != null) {
                break;
            }
            tmp = tmp.getSuperclass();
        }
        if (applicationException != null && (tmp.equals(ex.getClass()) || applicationException.inherited())) {
            return applicationException;
        }
        return null;
    }

    private boolean isBeanManaged(Class<?> declaringClass) {
        return declaringClass != null
                && declaringClass.getAnnotation(TransactionManagement.class) != null
                && declaringClass.getAnnotation(TransactionManagement.class).value() == TransactionManagementType.BEAN;
    }

}
