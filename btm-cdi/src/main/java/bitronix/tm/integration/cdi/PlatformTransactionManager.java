package bitronix.tm.integration.cdi;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;


import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import org.jglue.cdiunit.ProducesAlternative;

/**
 * Bitronix-specific Spring PlatformTransactionManager implementation.
 * 
 * @author Marcus Klimstra (CGI)
 */
@ApplicationScoped
public class PlatformTransactionManager {

    private BitronixTransactionManager transactionManager;;

    public PlatformTransactionManager() {
        this.transactionManager = TransactionManagerServices.getTransactionManager();
    }

    @PostConstruct
    public void postConstructPlatformTM() {
        // System.clearProperty("java.naming.factory.initial");
    }

    @PreDestroy
    public void preDestroyPlatfromTM() {
        transactionManager.shutdown();
        transactionManager = null;
    }

    @Produces
    @ProducesAlternative
    @Alternative
    protected UserTransaction retrieveUserTransaction() {
        return new TUserTransaction();
    }

    @Produces
    protected TransactionManager retrieveTransactionManager() {
        return transactionManager;
    }

    @Produces
    protected Object retrieveTransactionSynchronizationRegistry() {
        return transactionManager;
    }

    public void destroy() throws Exception {
        transactionManager.shutdown();
    }
}
