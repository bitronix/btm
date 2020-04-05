package bitronix.tm.integration.cdi;

import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.transaction.Transaction;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author aschoerk
 */
public class TransactionInfo {

    static class TAttribute {
        TransactionAttributeType ejbAttribue;
        Transactional.TxType txType;
        boolean userTransaction;
    }

    public TransactionInfo(TransactionInfo previous) {
        this.previous = previous;
        this.level = previous != null ? previous.level + 1 : 0;
    }

    List<EntityManagerInfo> entityManagers = new ArrayList<>();
    Transaction suspended;  // fetching of entitymanagers: only new ones
    boolean newTra = false; // if true: tra has been begin, entitymanagers joined, pop means: need to commit!
    TransactionAttributeType currentTransactionAttributeType;
    Transactional.TxType currentTxType;
    TAttribute tAttribute;
    TransactionInfo previous;
    boolean userTransaction;
    int level;

    public void setUserTransaction() {
        this.userTransaction = true;
    }

    public boolean isUserTransaction() {
        return userTransaction;
    }
}
