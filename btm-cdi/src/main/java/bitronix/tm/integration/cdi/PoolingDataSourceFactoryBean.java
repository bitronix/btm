package bitronix.tm.integration.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.resource.common.ResourceBean;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import bitronix.tm.utils.PropertyUtils;

/**
 * FactoryBean for PoolingDataSource to correctly manage its lifecycle when used
 * with Spring.
 * 
 * @author Marcus Klimstra (CGI)
 */
public class PoolingDataSourceFactoryBean extends ResourceBean {

    private static final Logger log = LoggerFactory.getLogger(PoolingDataSourceFactoryBean.class);
    private static final long serialVersionUID = 8283399886348754184L;

    private PoolingDataSource ds;

    public Class<PoolingDataSource> getObjectType() {
        return PoolingDataSource.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public PoolingDataSource getObject() throws Exception {
        if (ds == null) {
            ds = new PoolingDataSource();
            PropertyUtils.setProperties(ds, PropertyUtils.getProperties(this));


            log.debug("Initializing PoolingDataSource with id '{}'", ds.getUniqueName());
            ds.init();
        }
        return ds;
    }

    public void destroy() throws Exception {
        if (ds != null) {
            log.debug("Closing PoolingDataSource with id '{}'", ds.getUniqueName());
            ds.close();
            ds = null;
        }
    }
}
