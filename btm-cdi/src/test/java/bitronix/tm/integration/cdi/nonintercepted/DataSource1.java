package bitronix.tm.integration.cdi.nonintercepted;

import javax.enterprise.inject.Alternative;

import bitronix.tm.integration.cdi.PoolingDataSourceFactoryBean;

/**
 * @author aschoerk
 */
@Alternative
public class DataSource1 extends PoolingDataSourceFactoryBean {

    private static final long serialVersionUID = 6581338365140914540L;

    public DataSource1() {
        super();
        setClassName("bitronix.tm.mock.resource.jdbc.MockitoXADataSource");
        setUniqueName("btm-cdi-test-ds1");
        setMinPoolSize(1);
        setMaxPoolSize(3);
    }
}
