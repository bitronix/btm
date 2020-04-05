package bitronix.tm.integration.cdi.cdiintercepted;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.integration.cdi.SqlPersistenceFactory;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * @author aschoerk
 */
@ApplicationScoped
public class H2PersistenceFactory extends SqlPersistenceFactory {

    Logger log = LoggerFactory.getLogger("H2PersistenceFactory");

    public H2PersistenceFactory() {
    }


    @Override
    public String getPersistenceUnitName() {
        return "btm-cdi-test-h2-pu";
    }

    @Produces
    public EntityManager newEm() {
        return produceEntityManager();
    }


    @Produces
    @ApplicationScoped
    protected DataSource createDataSource() {
        if (ds != null)
            return ds;
        log.info("creating datasource");
        PoolingDataSource res = new PoolingDataSource();
        res.setClassName("org.h2.jdbcx.JdbcDataSource");
        Properties driverProperties = res.getDriverProperties();
        driverProperties.setProperty("URL", "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=0");
        driverProperties.setProperty("user","sa");
        driverProperties.setProperty("password","");
        res.setUniqueName("jdbc/btm-cdi-test-h2");
        res.setMinPoolSize(1);
        res.setMaxPoolSize(10);
        res.setAllowLocalTransactions(true);  // to allow autocommitmode
        res.init();
        log.info("created  datasource");
        return res;
    }



}
