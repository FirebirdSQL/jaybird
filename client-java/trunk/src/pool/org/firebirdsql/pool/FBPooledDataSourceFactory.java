package org.firebirdsql.pool;

import org.firebirdsql.gds.ClassFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

public class FBPooledDataSourceFactory {

    private static Logger log = LoggerFactory.getLogger(FBPooledDataSourceFactory.class, false);

    public static AbstractDriverConnectionPoolDataSource createDriverConnectionPoolDataSource() {
        try {
            return (AbstractDriverConnectionPoolDataSource) ClassFactory.get(
                    ClassFactory.DriverConnectionPoolDataSource).newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static AbstractFBConnectionPoolDataSource createFBConnectionPoolDataSource() {
        try {
            return (AbstractFBConnectionPoolDataSource) ClassFactory.get(
                    ClassFactory.FBConnectionPoolDataSource).newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
