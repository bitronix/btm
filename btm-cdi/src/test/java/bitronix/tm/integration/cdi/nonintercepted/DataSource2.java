package bitronix.tm.integration.cdi.nonintercepted;

import javax.enterprise.inject.Alternative;
import java.util.Properties;

import bitronix.tm.integration.cdi.PoolingDataSourceFactoryBean;

/**
 * @author aschoerk
 */
@Alternative
public class DataSource2 extends PoolingDataSourceFactoryBean {

    private static final long serialVersionUID = 6581338365140914540L;

    public DataSource2() {
        super();
        setClassName("bitronix.tm.mock.resource.jdbc.MockitoXADataSource");
        setUniqueName("btm-cdi-test-ds2");
        setMinPoolSize(1);
        setMaxPoolSize(2);
        setAutomaticEnlistingEnabled(true);
        setUseTmJoin(false);
        Properties driverProperties = new Properties();
        driverProperties.setProperty("loginTimeout", "5");
        setDriverProperties(driverProperties);
    }
}
