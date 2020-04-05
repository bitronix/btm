package bitronix.tm.integration.cdi.nonintercepted;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.sql.Connection;

import javax.inject.Inject;

import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.Assert;

@RunWith(CdiRunner.class)
@ActivatedAlternatives({DataSource2.class})
public class PoolingDataSourceFactoryBeanTest {

    @Inject
    private DataSource2 dataSource2;
    
    @Test
    public void validateProperties() {
        Assert.assertEquals("btm-cdi-test-ds2", dataSource2.getUniqueName());
        Assert.assertEquals("bitronix.tm.mock.resource.jdbc.MockitoXADataSource", dataSource2.getClassName());
        Assert.assertEquals(1, dataSource2.getMinPoolSize());
        Assert.assertEquals(2, dataSource2.getMaxPoolSize());
        Assert.assertEquals(true, dataSource2.getAutomaticEnlistingEnabled());
        Assert.assertEquals(false, dataSource2.getUseTmJoin());
        Assert.assertEquals(60, dataSource2.getMaxIdleTime()); // default value not overridden in bean configuration
        Assert.assertEquals("5", dataSource2.getDriverProperties().get("loginTimeout"));
    }
    
    @Test
    public void validateConnection() throws Exception {
        Connection connection = null;
        try {
            connection = dataSource2.getObject().getConnection();
            assertNotNull(connection);
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
